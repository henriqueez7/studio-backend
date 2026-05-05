package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.BarberAvailabilityRequest;
import br.com.studiohenriquecortes.dto.BarberAvailabilityResponse;
import br.com.studiohenriquecortes.entity.BarberAvailability;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.BarberAvailabilityRepository;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BarberAvailabilityService {

    private final BarberAvailabilityRepository barberAvailabilityRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BarberAvailabilityResponse> findByBarber(Long barberId, String authenticatedEmail) {
        validateBarberAccess(barberId, authenticatedEmail);

        return barberAvailabilityRepository.findByBarberIdOrderByDayOfWeekAsc(barberId)
                .stream()
                .sorted(Comparator.comparing(availability -> availability.getDayOfWeek().getValue()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BarberAvailabilityResponse create(BarberAvailabilityRequest request, String authenticatedEmail) {
        User barber = validateBarberAccess(request.getBarberId(), authenticatedEmail);
        validateAvailabilityRequest(request);

        if (barberAvailabilityRepository.existsByBarberIdAndDayOfWeek(barber.getId(), request.getDayOfWeek())) {
            throw new BusinessException("Ja existe disponibilidade cadastrada para esse barbeiro neste dia da semana.");
        }

        BarberAvailability availability = BarberAvailability.builder()
                .barber(barber)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotIntervalInMinutes(request.getSlotIntervalInMinutes())
                .active(request.getActive())
                .build();

        barberAvailabilityRepository.save(availability);

        return toResponse(availability);
    }

    @Transactional
    public BarberAvailabilityResponse update(Long id, BarberAvailabilityRequest request, String authenticatedEmail) {
        BarberAvailability availability = barberAvailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidade do barbeiro nao encontrada"));

        User barber = validateBarberAccess(request.getBarberId(), authenticatedEmail);
        validateAvailabilityRequest(request);

        if (!availability.getBarber().getId().equals(barber.getId())) {
            throw new BusinessException("O barbeiro informado nao corresponde ao registro de disponibilidade.");
        }

        boolean changingDay = availability.getDayOfWeek() != request.getDayOfWeek();
        if (changingDay && barberAvailabilityRepository.existsByBarberIdAndDayOfWeek(barber.getId(), request.getDayOfWeek())) {
            throw new BusinessException("Ja existe disponibilidade cadastrada para esse barbeiro neste dia da semana.");
        }

        availability.setBarber(barber);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setSlotIntervalInMinutes(request.getSlotIntervalInMinutes());
        availability.setActive(request.getActive());

        barberAvailabilityRepository.save(availability);

        return toResponse(availability);
    }

    @Transactional
    public void delete(Long id, String authenticatedEmail) {
        BarberAvailability availability = barberAvailabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidade do barbeiro nao encontrada"));

        validateBarberAccess(availability.getBarber().getId(), authenticatedEmail);
        barberAvailabilityRepository.delete(availability);
    }

    private User validateBarberAccess(Long barberId, String authenticatedEmail) {
        User authenticatedUser = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado nao encontrado"));

        User barber = userRepository.findById(barberId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro nao encontrado"));

        if (barber.getRole() != Role.BARBEIRO) {
            throw new BusinessException("O usuario informado nao possui perfil de barbeiro.");
        }

        if (authenticatedUser.getRole() == Role.BARBEIRO && !authenticatedUser.getId().equals(barberId)) {
            throw new AccessDeniedException("Voce so pode configurar a sua propria disponibilidade.");
        }

        return barber;
    }

    private void validateAvailabilityRequest(BarberAvailabilityRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException("O horario inicial deve ser menor que o horario final.");
        }
    }

    private BarberAvailabilityResponse toResponse(BarberAvailability availability) {
        return BarberAvailabilityResponse.builder()
                .id(availability.getId())
                .barberId(availability.getBarber().getId())
                .barberName(availability.getBarber().getName())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .slotIntervalInMinutes(availability.getSlotIntervalInMinutes())
                .active(availability.getActive())
                .build();
    }
}
