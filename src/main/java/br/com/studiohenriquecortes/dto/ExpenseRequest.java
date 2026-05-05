package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpenseRequest {

    @NotBlank(message = "A descrição da despesa é obrigatória.")
    @Size(max = 150, message = "A descrição da despesa deve ter no máximo 150 caracteres.")
    private String description;

    @NotBlank(message = "A categoria da despesa é obrigatória.")
    @Size(max = 80, message = "A categoria da despesa deve ter no máximo 80 caracteres.")
    private String category;

    @NotNull(message = "O valor da despesa é obrigatório.")
    @DecimalMin(value = "0.01", inclusive = true, message = "O valor da despesa deve ser maior que zero.")
    private BigDecimal amount;

    @NotNull(message = "A data da despesa é obrigatória.")
    private LocalDate expenseDate;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String notes;
}
