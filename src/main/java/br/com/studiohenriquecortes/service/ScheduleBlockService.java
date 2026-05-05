package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.ScheduleBlockRequest;
import br.com.studiohenriquecortes.dto.ScheduleBlockResponse;
import br.com.studiohenriquecortes.entity.Appointment;
import br.com.studiohenriquecortes.entity.ScheduleBlock;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.AppointmentStatus;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.AppointmentRepository;
import br.com.studiohenriquecortes.repository.ScheduleBlockRepository;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleBlockService {

    private final ScheduleBlockRepository scheduleBlockRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<ScheduleBlockResponse> findBlocks(LocalDate date, Long barberId, String authenticatedEmail) {
        if (date == null) {
            throw new BusinessException("A data do bloqueio e obrigatoria.");
        }

        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);
        Long effectiveBarberId = barberId;

        if (authenticatedUser.getRole() == Role.BARBEIRO) {
            effectiveBarberId = authenticatedUser.getId();
        }

        List<ScheduleBlock> blocks = effectiveBarberId != null
                ? scheduleBlockRepository.findDetailedByBarberIdAndBlockDate(effectiveBarberId, date)
                : scheduleBlockRepository.findDetailedByBlockDate(date);

        return blocks.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ScheduleBlockResponse create(ScheduleBlockRequest request, String authenticatedEmail) {
        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);
        validateOwnershipForWrite(authenticatedUser, request.getBarberId());
        normalizeRepeatRange(request);
        validateRequest(request);

        User barber = findBarberById(resolveBarberIdForWrite(authenticatedUser, request));
        validateNoConflicts(barber.getId(), request.getBlockDate(), request.getStartTime(), request.getEndTime(), null);

        ScheduleBlock mainBlock = buildBlock(request, barber);
        scheduleBlockRepository.save(mainBlock);
        createRepeatedBlocksIfNeeded(request, barber, null);
        return toResponse(mainBlock);
    }

    @Transactional
    public ScheduleBlockResponse update(Long blockId, ScheduleBlockRequest request, String authenticatedEmail) {
        ScheduleBlock existingBlock = scheduleBlockRepository.findDetailedById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Bloqueio nao encontrado."));

        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);
        validateExistingBlockAccess(authenticatedUser, existingBlock);
        validateOwnershipForWrite(authenticatedUser, request.getBarberId());
        normalizeRepeatRange(request);
        validateRequest(request);

        User barber = findBarberById(resolveBarberIdForWrite(authenticatedUser, request));
        validateNoConflicts(barber.getId(), request.getBlockDate(), request.getStartTime(), request.getEndTime(), blockId);

        existingBlock.setBarber(barber);
        existingBlock.setBlockDate(request.getBlockDate());
        existingBlock.setStartTime(request.getStartTime());
        existingBlock.setEndTime(request.getEndTime());
        existingBlock.setTitle(request.getTitle().trim());
        existingBlock.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        scheduleBlockRepository.save(existingBlock);
        createRepeatedBlocksIfNeeded(request, barber, blockId);
        return toResponse(existingBlock);
    }

    @Transactional
    public void delete(Long blockId, String authenticatedEmail) {
        ScheduleBlock block = scheduleBlockRepository.findDetailedById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("Bloqueio nao encontrado."));

        User authenticatedUser = findAuthenticatedUser(authenticatedEmail);
        validateExistingBlockAccess(authenticatedUser, block);
        scheduleBlockRepository.delete(block);
    }

    private void normalizeRepeatRange(ScheduleBlockRequest request) {
        if (request.getRepeatStart() == null) {
            request.setRepeatStart(request.getBlockDate());
        }
    }

    private void createRepeatedBlocksIfNeeded(ScheduleBlockRequest request, User barber, Long ignoredBlockId) {
        List<LocalDate> repeatedDates = buildRepeatedDates(request);

        for (LocalDate repeatedDate : repeatedDates) {
            validateNoConflicts(barber.getId(), repeatedDate, request.getStartTime(), request.getEndTime(), ignoredBlockId);

            ScheduleBlock repeatedBlock = ScheduleBlock.builder()
                    .barber(barber)
                    .blockDate(repeatedDate)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .title(request.getTitle().trim())
                    .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                    .build();

            scheduleBlockRepository.save(repeatedBlock);
        }
    }

    private List<LocalDate> buildRepeatedDates(ScheduleBlockRequest request) {
        if (request.getRepeatWeekdays() == null || request.getRepeatWeekdays().isEmpty()) {
            return List.of();
        }

        if (request.getRepeatStart() == null || request.getRepeatUntil() == null) {
            return List.of();
        }

        if (request.getRepeatUntil().isBefore(request.getRepeatStart())) {
            return List.of();
        }

        Set<DayOfWeek> weekdays = new HashSet<>(request.getRepeatWeekdays());
        List<LocalDate> dates = new java.util.ArrayList<>();

        for (LocalDate cursor = request.getRepeatStart();
             !cursor.isAfter(request.getRepeatUntil());
             cursor = cursor.plusDays(1)) {
            if (cursor.equals(request.getBlockDate())) {
                continue;
            }

            if (weekdays.contains(cursor.getDayOfWeek())) {
                dates.add(cursor);
            }
        }

        return dates;
    }

    private Long resolveBarberIdForWrite(User authenticatedUser, ScheduleBlockRequest request) {
        if (authenticatedUser.getRole() == Role.BARBEIRO) {
            request.setBarberId(authenticatedUser.getId());
            return authenticatedUser.getId();
        }
        return request.getBarberId();
    }

    private void validateOwnershipForWrite(User authenticatedUser, Long requestedBarberId) {
        if (authenticatedUser.getRole() == Role.BARBEIRO
                && requestedBarberId != null
                && !authenticatedUser.getId().equals(requestedBarberId)) {
            throw new AccessDeniedException("Voce so pode criar ou editar bloqueios na sua propria agenda.");
        }
    }

    private void validateExistingBlockAccess(User authenticatedUser, ScheduleBlock block) {
        if (authenticatedUser.getRole() == Role.BARBEIRO
                && !authenticatedUser.getId().equals(block.getBarber().getId())) {
            throw new AccessDeniedException("Voce so pode alterar bloqueios da sua propria agenda.");
        }
    }

    private ScheduleBlock buildBlock(ScheduleBlockRequest request, User barber) {
        return ScheduleBlock.builder()
                .barber(barber)
                .blockDate(request.getBlockDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .title(request.getTitle().trim())
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .build();
    }

    private void validateRequest(ScheduleBlockRequest request) {
        if (request.getBlockDate() == null) {
            throw new BusinessException("A data do bloqueio e obrigatoria.");
        }

        if (request.getBlockDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Nao e permitido bloquear uma data passada.");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException("Os horarios de inicio e fim sao obrigatorios.");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("O horario final deve ser maior que o horario inicial.");
        }

        if (request.getRepeatStart() != null && request.getRepeatStart().isBefore(request.getBlockDate())) {
            throw new BusinessException("A data inicial da repeticao deve ser igual ou posterior ao dia principal.");
        }

        if (request.getRepeatWeekdays() != null
                && !request.getRepeatWeekdays().isEmpty()
                && request.getRepeatUntil() == null) {
            throw new BusinessException("Defina a data final para repetir o bloqueio.");
        }

        if (request.getRepeatUntil() != null && request.getRepeatStart() != null
                && request.getRepeatUntil().isBefore(request.getRepeatStart())) {
            throw new BusinessException("A data final da repeticao deve ser igual ou posterior a data inicial.");
        }

        if (request.getRepeatUntil() != null
                && (request.getRepeatWeekdays() == null || request.getRepeatWeekdays().isEmpty())) {
            throw new BusinessException("Selecione ao menos um dia da semana para repetir o bloqueio.");
        }
    }

    private void validateNoConflicts(Long barberId, LocalDate date, LocalTime startTime, LocalTime endTime, Long ignoredBlockId) {
        List<ScheduleBlock> existingBlocks = scheduleBlockRepository.findDetailedByBarberIdAndBlockDate(barberId, date);
        boolean conflictsWithBlock = existingBlocks.stream()
                .filter(block -> ignoredBlockId == null || !ignoredBlockId.equals(block.getId()))
                .anyMatch(block -> hasTimeConflict(startTime, endTime, block.getStartTime(), block.getEndTime()));

        if (conflictsWithBlock) {
            throw new BusinessException("Ja existe um bloqueio nesse intervalo.");
        }

        List<Appointment> appointments = appointmentRepository.findDetailedByBarberIdAndAppointmentDate(barberId, date);
        boolean conflictsWithAppointment = appointments.stream()
                .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELADO)
                .anyMatch(appointment -> hasTimeConflict(startTime, endTime, appointment.getStartTime(), appointment.getEndTime()));

        if (conflictsWithAppointment) {
            throw new BusinessException("Ja existe um atendimento nesse intervalo.");
        }
    }

    private boolean hasTimeConflict(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        if (startA == null || endA == null || startB == null || endB == null) {
            return false;
        }

        return startA.isBefore(endB) && endA.isAfter(startB);
    }

    private User findAuthenticatedUser(String authenticatedEmail) {
        return userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado nao encontrado"));
    }

    private User findBarberById(Long barberId) {
        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro nao encontrado"));

        if (barber.getRole() != Role.BARBEIRO) {
            throw new BusinessException("O usuario informado nao possui perfil de barbeiro.");
        }

        if (!Boolean.TRUE.equals(barber.getActive())) {
            throw new BusinessException("O barbeiro informado esta inativo.");
        }

        return barber;
    }

    private ScheduleBlockResponse toResponse(ScheduleBlock block) {
        return ScheduleBlockResponse.builder()
                .id(block.getId())
                .barberId(block.getBarber() != null ? block.getBarber().getId() : null)
                .barberName(block.getBarber() != null ? block.getBarber().getName() : null)
                .blockDate(block.getBlockDate())
                .startTime(block.getStartTime())
                .endTime(block.getEndTime())
                .title(block.getTitle())
                .notes(block.getNotes())
                .build();
    }
}
