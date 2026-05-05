package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class AppointmentRequest {

    @NotNull(message = "O cliente e obrigatorio.")
    private Long clientId;

    @NotNull(message = "O barbeiro e obrigatorio.")
    private Long barberId;

    @NotEmpty(message = "Selecione pelo menos um servico.")
    private List<Long> serviceIds;

    @NotNull(message = "A data do agendamento e obrigatoria.")
    private LocalDate appointmentDate;

    @NotNull(message = "O horario inicial e obrigatorio.")
    private LocalTime startTime;

    @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres.")
    private String notes;
}
