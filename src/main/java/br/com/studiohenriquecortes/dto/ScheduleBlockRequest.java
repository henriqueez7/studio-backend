package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class ScheduleBlockRequest {

    @NotNull(message = "O barbeiro e obrigatorio.")
    private Long barberId;

    @NotNull(message = "A data do bloqueio e obrigatoria.")
    private LocalDate blockDate;

    @NotNull(message = "O horario inicial e obrigatorio.")
    private LocalTime startTime;

    @NotNull(message = "O horario final e obrigatorio.")
    private LocalTime endTime;

    @NotBlank(message = "O titulo do bloqueio e obrigatorio.")
    @Size(max = 120, message = "O titulo deve ter no maximo 120 caracteres.")
    private String title;

    @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres.")
    private String notes;

    private List<DayOfWeek> repeatWeekdays;

    private LocalDate repeatStart;

    private LocalDate repeatUntil;
}
