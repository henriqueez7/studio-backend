package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.AppointmentPersonResponse;
import br.com.studiohenriquecortes.dto.AppointmentRequest;
import br.com.studiohenriquecortes.dto.AppointmentResponse;
import br.com.studiohenriquecortes.dto.AppointmentServiceItemResponse;
import br.com.studiohenriquecortes.dto.AvailableDateResponse;
import br.com.studiohenriquecortes.dto.AvailableTimeResponse;
import br.com.studiohenriquecortes.entity.Appointment;
import br.com.studiohenriquecortes.entity.AppointmentNotificationSettings;
import br.com.studiohenriquecortes.entity.AppointmentServiceItem;
import br.com.studiohenriquecortes.entity.BarberAvailability;
import br.com.studiohenriquecortes.entity.BarbershopService;
import br.com.studiohenriquecortes.entity.ScheduleBlock;
import br.com.studiohenriquecortes.entity.StoreBusinessHour;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.AppointmentStatus;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.AppointmentRepository;
import br.com.studiohenriquecortes.repository.BarberAvailabilityRepository;
import br.com.studiohenriquecortes.repository.BarbershopServiceRepository;
import br.com.studiohenriquecortes.repository.ScheduleBlockRepository;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final int DEFAULT_AVAILABILITY_DAYS_AHEAD = 30;

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BarbershopServiceRepository serviceRepository;
    private final BarberAvailabilityRepository barberAvailabilityRepository;
    private final ScheduleBlockRepository scheduleBlockRepository;
    private final StoreBusinessHourService storeBusinessHourService;
    private final AppointmentNotificationSettingsService appointmentNotificationSettingsService;
    private final WhatsappNotificationService whatsappNotificationService;

    @Transactional
    public AppointmentResponse create(AppointmentRequest request, String authenticatedEmail) {
        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);

        Long clientId = request.getClientId();
        if (authenticatedUser.getRole() == Role.CLIENTE) {
            clientId = authenticatedUser.getId();
        }

        if (authenticatedUser.getRole() == Role.BARBEIRO && !authenticatedUser.getId().equals(request.getBarberId())) {
            throw new AccessDeniedException("Voce so pode criar agendamentos na sua propria agenda.");
        }

        User client = findClientById(clientId);
        User barber = findBarberById(request.getBarberId());
        List<BarbershopService> selectedServices = findActiveServicesByIds(request.getServiceIds());

        validateAppointmentDateTime(request.getAppointmentDate(), request.getStartTime());

        List<AvailableTimeResponse> availableTimes = findAvailableTimes(
                barber.getId(),
                request.getAppointmentDate(),
                request.getServiceIds()
        );

        boolean requestedTimeIsAvailable = availableTimes.stream()
                .anyMatch(time -> time.getStartTime().equals(request.getStartTime()));

        if (!requestedTimeIsAvailable) {
            throw new BusinessException("O horario informado nao esta disponivel para os servicos selecionados.");
        }

        int totalDurationInMinutes = calculateTotalDurationInMinutes(selectedServices);
        BigDecimal totalPrice = calculateTotalPrice(selectedServices);
        LocalTime endTime = request.getStartTime().plusMinutes(totalDurationInMinutes);

        Appointment appointment = Appointment.builder()
                .client(client)
                .barber(barber)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .totalDurationInMinutes(totalDurationInMinutes)
                .totalPrice(totalPrice)
                .status(AppointmentStatus.PENDENTE)
                .notes(request.getNotes())
                .build();

        for (int index = 0; index < selectedServices.size(); index++) {
            BarbershopService service = selectedServices.get(index);
            appointment.addItem(AppointmentServiceItem.builder()
                    .service(service)
                    .position(index)
                    .serviceNameSnapshot(service.getName())
                    .servicePriceSnapshot(service.getPrice())
                    .serviceDurationInMinutesSnapshot(service.getDurationInMinutes())
                    .build());
        }

        appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAllDetailed()
                .stream()
                .sorted(buildAppointmentComparator())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByBarber(Long barberId, String authenticatedEmail) {
        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);

        if (authenticatedUser.getRole() == Role.BARBEIRO && !authenticatedUser.getId().equals(barberId)) {
            throw new AccessDeniedException("Voce so pode visualizar a sua propria agenda.");
        }

        findBarberById(barberId);

        return appointmentRepository.findDetailedByBarberId(barberId)
                .stream()
                .sorted(buildAppointmentComparator())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByClient(Long clientId, String authenticatedEmail) {
        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);

        if (authenticatedUser.getRole() == Role.CLIENTE && !authenticatedUser.getId().equals(clientId)) {
            throw new AccessDeniedException("Voce so pode visualizar os seus proprios agendamentos.");
        }

        findClientById(clientId);

        return appointmentRepository.findDetailedByClientId(clientId)
                .stream()
                .sorted(buildAppointmentComparator())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByAuthenticatedClient(String authenticatedEmail) {
        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);

        if (authenticatedUser.getRole() != Role.CLIENTE) {
            throw new AccessDeniedException("Apenas clientes podem visualizar seus proprios agendamentos.");
        }

        return appointmentRepository.findDetailedByClientId(authenticatedUser.getId())
                .stream()
                .sorted(buildAppointmentComparator())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailableDateResponse> findAvailableDates(Long barberId, List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new BusinessException("Informe os servicos para calcular as datas disponiveis.");
        }

        User barber = findBarberById(barberId);
        List<BarbershopService> selectedServices = findAvailableServicesByIds(serviceIds);
        if (selectedServices.isEmpty()) {
            return List.of();
        }

        List<AvailableDateResponse> availableDates = new ArrayList<>();
        LocalDate startDate = LocalDate.now();

        for (int i = 0; i < DEFAULT_AVAILABILITY_DAYS_AHEAD; i++) {
            LocalDate date = startDate.plusDays(i);
            if (hasAvailabilityForDate(barber, date, selectedServices)) {
                availableDates.add(AvailableDateResponse.builder().date(date).build());
            }
        }

        return availableDates;
    }

    @Transactional(readOnly = true)
    public List<AvailableTimeResponse> findAvailableTimes(Long barberId, LocalDate date, List<Long> serviceIds) {
        return findAvailableTimes(barberId, date, serviceIds, null);
    }

    @Transactional(readOnly = true)
    public List<AvailableTimeResponse> findAvailableTimes(Long barberId, LocalDate date, List<Long> serviceIds, Long ignoredAppointmentId) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new BusinessException("Informe os servicos para calcular os horarios disponiveis.");
        }

        User barber = findBarberById(barberId);
        List<BarbershopService> selectedServices = findAvailableServicesByIds(serviceIds);
        if (selectedServices.isEmpty()) {
            return List.of();
        }

        return calculateAvailableTimes(barber, date, selectedServices, ignoredAppointmentId);
    }

    @Transactional
    public AppointmentResponse update(Long appointmentId, AppointmentRequest request, String authenticatedEmail) {
        Appointment appointment = findAppointmentById(appointmentId);
        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);
        validateBarberOwnershipIfNecessary(appointment, authenticatedEmail);

        if (authenticatedUser.getRole() == Role.BARBEIRO && !authenticatedUser.getId().equals(request.getBarberId())) {
            throw new AccessDeniedException("Voce so pode mover agendamentos para a sua propria agenda.");
        }

        User client = findClientById(request.getClientId());
        User barber = findBarberById(request.getBarberId());
        List<BarbershopService> selectedServices = findActiveServicesByIds(request.getServiceIds());

        validateAppointmentDateTime(request.getAppointmentDate(), request.getStartTime());

        List<AvailableTimeResponse> availableTimes = calculateAvailableTimes(
                barber,
                request.getAppointmentDate(),
                selectedServices,
                appointment.getId()
        );

        boolean requestedTimeIsAvailable = availableTimes.stream()
                .anyMatch(time -> time.getStartTime().equals(request.getStartTime()));

        if (!requestedTimeIsAvailable) {
            throw new BusinessException("O horario informado nao esta disponivel para os servicos selecionados.");
        }

        int totalDurationInMinutes = calculateTotalDurationInMinutes(selectedServices);
        BigDecimal totalPrice = calculateTotalPrice(selectedServices);
        LocalTime endTime = request.getStartTime().plusMinutes(totalDurationInMinutes);

        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(endTime);
        appointment.setTotalDurationInMinutes(totalDurationInMinutes);
        appointment.setTotalPrice(totalPrice);
        appointment.setNotes(request.getNotes());
        appointment.clearItems();

        for (int index = 0; index < selectedServices.size(); index++) {
            BarbershopService service = selectedServices.get(index);
            appointment.addItem(AppointmentServiceItem.builder()
                    .service(service)
                    .position(index)
                    .serviceNameSnapshot(service.getName())
                    .servicePriceSnapshot(service.getPrice())
                    .serviceDurationInMinutesSnapshot(service.getDurationInMinutes())
                    .build());
        }

        appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Transactional
    public void delete(Long appointmentId, String authenticatedEmail) {
        Appointment appointment = findAppointmentById(appointmentId);
        validateBarberOwnershipIfNecessary(appointment, authenticatedEmail);
        appointmentRepository.delete(appointment);
    }

    @Transactional
    public AppointmentResponse confirm(Long appointmentId, String authenticatedEmail) {
        Appointment appointment = findAppointmentById(appointmentId);
        validateBarberOwnershipIfNecessary(appointment, authenticatedEmail);

        if (appointment.getStatus() == AppointmentStatus.CANCELADO) {
            throw new BusinessException("Nao e possivel confirmar um agendamento cancelado.");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMADO);
        appointmentRepository.save(appointment);
        AppointmentNotificationSettings settings = appointmentNotificationSettingsService.loadOrCreateDefault();
        whatsappNotificationService.sendConfirmation(appointment, settings);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(Long appointmentId, String authenticatedEmail) {
        Appointment appointment = findAppointmentById(appointmentId);
        validateBarberOwnershipIfNecessary(appointment, authenticatedEmail);

        if (appointment.getStatus() == AppointmentStatus.CONCLUIDO) {
            throw new BusinessException("Nao e possivel cancelar um agendamento concluido.");
        }

        appointment.setStatus(AppointmentStatus.CANCELADO);
        appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse finish(Long appointmentId, String authenticatedEmail) {
        Appointment appointment = findAppointmentById(appointmentId);
        validateBarberOwnershipIfNecessary(appointment, authenticatedEmail);

        if (appointment.getStatus() == AppointmentStatus.CANCELADO) {
            throw new BusinessException("Nao e possivel concluir um agendamento cancelado.");
        }

        appointment.setStatus(AppointmentStatus.CONCLUIDO);
        appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    private boolean hasAvailabilityForDate(User barber, LocalDate date, List<BarbershopService> selectedServices) {
        return !calculateAvailableTimes(barber, date, selectedServices, null).isEmpty();
    }

    private List<AvailableTimeResponse> calculateAvailableTimes(User barber, LocalDate date, List<BarbershopService> selectedServices, Long ignoredAppointmentId) {
        validateAvailabilityDate(date);

        java.util.Optional<BarberAvailability> activeAvailability = findActiveAvailability(barber.getId(), date);
        if (activeAvailability.isEmpty()) {
            return List.of();
        }

        BarberAvailability availability = activeAvailability.get();
        StoreBusinessHour storeBusinessHour = storeBusinessHourService.findActiveByDay(date.getDayOfWeek());
        if (storeBusinessHour == null) {
            return List.of();
        }

        LocalTime effectiveStartTime = availability.getStartTime().isAfter(storeBusinessHour.getStartTime())
                ? availability.getStartTime()
                : storeBusinessHour.getStartTime();
        LocalTime effectiveEndTime = availability.getEndTime().isBefore(storeBusinessHour.getEndTime())
                ? availability.getEndTime()
                : storeBusinessHour.getEndTime();

        if (!effectiveEndTime.isAfter(effectiveStartTime)) {
            return List.of();
        }

        int totalDurationInMinutes = selectedServices.isEmpty()
                ? availability.getSlotIntervalInMinutes()
                : calculateTotalDurationInMinutes(selectedServices);
        if (totalDurationInMinutes <= 0) {
            return List.of();
        }

        Integer slotIntervalInMinutes = availability.getSlotIntervalInMinutes();
        if (slotIntervalInMinutes == null || slotIntervalInMinutes <= 0) {
            return List.of();
        }

        LocalTime latestStartTime = effectiveEndTime.minusMinutes(totalDurationInMinutes);
        if (latestStartTime.isBefore(effectiveStartTime)) {
            return List.of();
        }

        List<Appointment> existingAppointments = appointmentRepository.findDetailedByBarberIdAndAppointmentDate(
                barber.getId(),
                date
        );
        List<ScheduleBlock> existingBlocks = scheduleBlockRepository.findDetailedByBarberIdAndBlockDate(
                barber.getId(),
                date
        );

        List<AvailableTimeResponse> availableTimes = new ArrayList<>();
        LocalTime candidateStart = effectiveStartTime;

        while (!candidateStart.isAfter(latestStartTime)) {
            LocalTime currentStart = candidateStart;
            LocalTime currentEnd = currentStart.plusMinutes(totalDurationInMinutes);

            boolean notPastTime = !date.equals(LocalDate.now()) || !currentStart.isBefore(LocalTime.now());
            boolean noAppointmentConflict = existingAppointments.stream()
                    .filter(Predicate.not(this::isCanceledAppointment))
                    .filter(this::hasValidTimeRange)
                    .filter(appointment -> ignoredAppointmentId == null || !ignoredAppointmentId.equals(appointment.getId()))
                    .noneMatch(appointment -> hasTimeConflict(
                            currentStart,
                            currentEnd,
                            appointment.getStartTime(),
                            appointment.getEndTime()
                    ));
            boolean noBlockConflict = existingBlocks.stream()
                    .noneMatch(block -> hasTimeConflict(
                            currentStart,
                            currentEnd,
                            block.getStartTime(),
                            block.getEndTime()
                    ));

            if (notPastTime && noAppointmentConflict && noBlockConflict) {
                availableTimes.add(AvailableTimeResponse.builder()
                        .startTime(currentStart)
                        .endTime(currentEnd)
                        .totalDurationInMinutes(totalDurationInMinutes)
                        .build());
            }

            candidateStart = candidateStart.plusMinutes(slotIntervalInMinutes);
        }

        return availableTimes;
    }

    private boolean hasTimeConflict(LocalTime requestedStartTime, LocalTime requestedEndTime, LocalTime existingStartTime, LocalTime existingEndTime) {
        if (requestedStartTime == null || requestedEndTime == null || existingStartTime == null || existingEndTime == null) {
            return false;
        }

        return requestedStartTime.isBefore(existingEndTime) && requestedEndTime.isAfter(existingStartTime);
    }

    private boolean hasValidTimeRange(Appointment appointment) {
        return appointment.getStartTime() != null && appointment.getEndTime() != null;
    }

    private boolean isCanceledAppointment(Appointment appointment) {
        return appointment.getStatus() == AppointmentStatus.CANCELADO;
    }

    private User findAuthenticatedUser(String authenticatedEmail) {
        return userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado nao encontrado"));
    }

    private User findClientById(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));

        if (client.getRole() != Role.CLIENTE) {
            throw new BusinessException("O usuario informado como cliente nao possui perfil de cliente.");
        }

        if (!Boolean.TRUE.equals(client.getActive())) {
            throw new BusinessException("O cliente informado esta inativo.");
        }

        return client;
    }

    private User findBarberById(Long barberId) {
        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro nao encontrado"));

        if (barber.getRole() != Role.BARBEIRO) {
            throw new BusinessException("O usuario informado como barbeiro nao possui perfil de barbeiro.");
        }

        if (!Boolean.TRUE.equals(barber.getActive())) {
            throw new BusinessException("O barbeiro informado esta inativo.");
        }

        return barber;
    }

    private List<BarbershopService> findActiveServicesByIds(List<Long> serviceIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>(serviceIds);
        if (uniqueIds.isEmpty()) {
            throw new BusinessException("Selecione pelo menos um servico.");
        }

        List<BarbershopService> activeServices = serviceRepository.findByIdInAndActiveTrue(new ArrayList<>(uniqueIds));
        if (activeServices.size() != uniqueIds.size()) {
            throw new BusinessException("Um ou mais servicos informados nao existem ou estao inativos.");
        }

        Map<Long, BarbershopService> servicesById = activeServices.stream()
                .collect(Collectors.toMap(BarbershopService::getId, service -> service));

        return uniqueIds.stream().map(servicesById::get).toList();
    }

    private List<BarbershopService> findAvailableServicesByIds(List<Long> serviceIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>(serviceIds);
        if (uniqueIds.isEmpty()) {
            return List.of();
        }

        List<BarbershopService> activeServices = serviceRepository.findByIdInAndActiveTrue(new ArrayList<>(uniqueIds));
        if (activeServices.size() != uniqueIds.size()) {
            return List.of();
        }

        Map<Long, BarbershopService> servicesById = activeServices.stream()
                .filter(service -> service.getDurationInMinutes() != null && service.getDurationInMinutes() > 0)
                .collect(Collectors.toMap(BarbershopService::getId, service -> service));

        if (servicesById.size() != uniqueIds.size()) {
            return List.of();
        }

        return uniqueIds.stream().map(servicesById::get).toList();
    }

    private void validateAppointmentDateTime(LocalDate appointmentDate, LocalTime startTime) {
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Nao e permitido agendar em uma data passada.");
        }

        if (appointmentDate.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            throw new BusinessException("Nao e permitido agendar em um horario passado.");
        }
    }

    private void validateAvailabilityDate(LocalDate date) {
        if (date == null) {
            throw new BusinessException("A data informada e obrigatoria.");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException("Nao e permitido consultar disponibilidade em data passada.");
        }
    }

    private java.util.Optional<BarberAvailability> findActiveAvailability(Long barberId, LocalDate date) {
        return barberAvailabilityRepository.findByBarberIdAndDayOfWeek(barberId, date.getDayOfWeek())
                .filter(availability -> Boolean.TRUE.equals(availability.getActive()));
    }

    private int calculateTotalDurationInMinutes(List<BarbershopService> services) {
        return services.stream().map(BarbershopService::getDurationInMinutes).reduce(0, Integer::sum);
    }

    private BigDecimal calculateTotalPrice(List<BarbershopService> services) {
        return services.stream().map(BarbershopService::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateBarberOwnershipIfNecessary(Appointment appointment, String authenticatedEmail) {
        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);

        if (authenticatedUser.getRole() == Role.BARBEIRO
                && !appointment.getBarber().getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("Voce so pode alterar os seus proprios atendimentos.");
        }
    }

    private Appointment findAppointmentById(Long appointmentId) {
        return appointmentRepository.findDetailedById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento nao encontrado"));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        List<AppointmentServiceItemResponse> services = safeItems(appointment).stream()
                .sorted(Comparator.comparing(
                        AppointmentServiceItem::getPosition,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(item -> AppointmentServiceItemResponse.builder()
                        .id(item.getService() != null ? item.getService().getId() : null)
                        .name(item.getServiceNameSnapshot())
                        .serviceId(item.getService() != null ? item.getService().getId() : null)
                        .serviceName(item.getServiceNameSnapshot())
                        .durationInMinutes(item.getServiceDurationInMinutesSnapshot())
                        .price(item.getServicePriceSnapshot())
                        .position(item.getPosition())
                        .build())
                .toList();

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .appointmentDate(appointment.getAppointmentDate())
                .client(toAppointmentPersonResponse(appointment.getClient()))
                .barber(toAppointmentPersonResponse(appointment.getBarber()))
                .clientId(appointment.getClient() != null ? appointment.getClient().getId() : null)
                .clientName(appointment.getClient() != null ? appointment.getClient().getName() : null)
                .barberId(appointment.getBarber() != null ? appointment.getBarber().getId() : null)
                .barberName(appointment.getBarber() != null ? appointment.getBarber().getName() : null)
                .services(services)
                .date(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .totalDurationInMinutes(appointment.getTotalDurationInMinutes())
                .totalPrice(appointment.getTotalPrice())
                .notes(appointment.getNotes())
                .status(appointment.getStatus() != null ? appointment.getStatus().name() : null)
                .build();
    }

    private AppointmentPersonResponse toAppointmentPersonResponse(User user) {
        if (user == null) {
            return null;
        }

        return AppointmentPersonResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .active(user.getActive())
                .build();
    }

    private Comparator<Appointment> buildAppointmentComparator() {
        return Comparator.comparing(Appointment::getAppointmentDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Appointment::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Appointment::getId);
    }

    private List<AppointmentServiceItem> safeItems(Appointment appointment) {
        if (appointment == null || appointment.getItems() == null) {
            return Collections.emptyList();
        }

        return appointment.getItems().stream()
                .filter(Objects::nonNull)
                .toList();
    }
}

