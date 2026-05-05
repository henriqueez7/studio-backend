package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
public class StoreBusinessHourRequest {

    @NotNull(message = "O dia da semana e obrigatorio.")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "O horario inicial e obrigatorio.")
    private LocalTime startTime;

    @NotNull(message = "O horario final e obrigatorio.")
    private LocalTime endTime;

    @NotNull(message = "O status ativo/inativo e obrigatorio.")
    private Boolean active;
}
