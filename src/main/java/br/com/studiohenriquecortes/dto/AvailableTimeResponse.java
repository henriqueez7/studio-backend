package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class AvailableTimeResponse {

    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalDurationInMinutes;
}
