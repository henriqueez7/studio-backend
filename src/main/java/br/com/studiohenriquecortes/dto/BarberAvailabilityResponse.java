package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Builder
public class BarberAvailabilityResponse {

    private Long id;
    private Long barberId;
    private String barberName;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotIntervalInMinutes;
    private Boolean active;
}
