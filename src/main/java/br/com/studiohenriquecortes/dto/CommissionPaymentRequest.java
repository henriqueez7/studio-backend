package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CommissionPaymentRequest {

    @NotNull(message = "O barbeiro é obrigatório.")
    private Long barberId;

    @NotNull(message = "A data inicial do período é obrigatória.")
    private LocalDate periodStart;

    @NotNull(message = "A data final do período é obrigatória.")
    private LocalDate periodEnd;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String notes;
}
