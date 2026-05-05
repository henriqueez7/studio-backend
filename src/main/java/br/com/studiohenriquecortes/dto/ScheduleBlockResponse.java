package br.com.studiohenriquecortes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleBlockResponse {

    private Long id;
    private Long barberId;
    private String barberName;
    private LocalDate blockDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String title;
    private String notes;
}
