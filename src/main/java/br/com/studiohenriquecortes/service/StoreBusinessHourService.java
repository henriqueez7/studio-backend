package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.StoreBusinessHourRequest;
import br.com.studiohenriquecortes.dto.StoreBusinessHourResponse;
import br.com.studiohenriquecortes.entity.StoreBusinessHour;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.StoreBusinessHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreBusinessHourService {

    private final StoreBusinessHourRepository storeBusinessHourRepository;

    @Transactional(readOnly = true)
    public List<StoreBusinessHourResponse> findAll() {
        return ensureDefaults().stream()
                .sorted(Comparator.comparing(item -> item.getDayOfWeek().getValue()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StoreBusinessHourResponse create(StoreBusinessHourRequest request) {
        validateRequest(request);

        if (storeBusinessHourRepository.findByDayOfWeek(request.getDayOfWeek()).isPresent()) {
            throw new BusinessException("Ja existe expediente cadastrado para este dia.");
        }

        StoreBusinessHour storeBusinessHour = storeBusinessHourRepository.save(
                StoreBusinessHour.builder()
                        .dayOfWeek(request.getDayOfWeek())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .active(request.getActive())
                        .build()
        );

        return toResponse(storeBusinessHour);
    }

    @Transactional
    public StoreBusinessHourResponse update(Long id, StoreBusinessHourRequest request) {
        validateRequest(request);

        StoreBusinessHour existing = storeBusinessHourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expediente da loja nao encontrado."));

        storeBusinessHourRepository.findByDayOfWeek(request.getDayOfWeek())
                .filter(item -> !item.getId().equals(id))
                .ifPresent(item -> {
                    throw new BusinessException("Ja existe expediente cadastrado para este dia.");
                });

        existing.setDayOfWeek(request.getDayOfWeek());
        existing.setStartTime(request.getStartTime());
        existing.setEndTime(request.getEndTime());
        existing.setActive(request.getActive());

        return toResponse(storeBusinessHourRepository.save(existing));
    }

    @Transactional(readOnly = true)
    public StoreBusinessHour findActiveByDay(DayOfWeek dayOfWeek) {
        ensureDefaults();
        return storeBusinessHourRepository.findByDayOfWeek(dayOfWeek)
                .filter(item -> Boolean.TRUE.equals(item.getActive()))
                .orElse(null);
    }

    @Transactional
    public List<StoreBusinessHour> ensureDefaults() {
        List<StoreBusinessHour> existing = storeBusinessHourRepository.findAllByOrderByDayOfWeekAsc();
        if (existing.size() == DayOfWeek.values().length) {
            return existing;
        }

        for (DayOfWeek dayOfWeek : Arrays.asList(DayOfWeek.values())) {
            storeBusinessHourRepository.findByDayOfWeek(dayOfWeek)
                    .orElseGet(() -> storeBusinessHourRepository.save(defaultFor(dayOfWeek)));
        }

        return storeBusinessHourRepository.findAllByOrderByDayOfWeekAsc();
    }

    private StoreBusinessHour defaultFor(DayOfWeek dayOfWeek) {
        boolean active = switch (dayOfWeek) {
            case TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY -> true;
            default -> false;
        };

        return StoreBusinessHour.builder()
                .dayOfWeek(dayOfWeek)
                .startTime(java.time.LocalTime.of(9, 0))
                .endTime(java.time.LocalTime.of(20, 0))
                .active(active)
                .build();
    }

    private void validateRequest(StoreBusinessHourRequest request) {
        if (request.getEndTime() != null
                && request.getStartTime() != null
                && !request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("O horario final deve ser maior que o horario inicial.");
        }
    }

    private StoreBusinessHourResponse toResponse(StoreBusinessHour storeBusinessHour) {
        return StoreBusinessHourResponse.builder()
                .id(storeBusinessHour.getId())
                .dayOfWeek(storeBusinessHour.getDayOfWeek())
                .startTime(storeBusinessHour.getStartTime())
                .endTime(storeBusinessHour.getEndTime())
                .active(storeBusinessHour.getActive())
                .build();
    }
}
