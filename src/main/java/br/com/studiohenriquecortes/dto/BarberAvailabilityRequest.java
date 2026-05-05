package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
public class BarberAvailabilityRequest {

    @NotNull(message = "O barbeiro e obrigatorio.")
    private Long barberId;

    @NotNull(message = "O dia da semana e obrigatorio.")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "O horario inicial e obrigatorio.")
    private LocalTime startTime;

    @NotNull(message = "O horario final e obrigatorio.")
    private LocalTime endTime;

    @NotNull(message = "O intervalo entre atendimentos e obrigatorio.")
    @Min(value = 1, message = "O intervalo entre atendimentos deve ser maior que zero.")
    private Integer slotIntervalInMinutes;

    @NotNull(message = "O status ativo/inativo e obrigatorio.")
    private Boolean active;
}
