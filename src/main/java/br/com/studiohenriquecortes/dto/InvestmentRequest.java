package br.com.studiohenriquecortes.dto;

import br.com.studiohenriquecortes.enums.InvestmentPriority;
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
public class InvestmentRequest {

    @NotBlank(message = "O título do investimento é obrigatório.")
    @Size(max = 150, message = "O título do investimento deve ter no máximo 150 caracteres.")
    private String title;

    @Size(max = 2000, message = "A descrição do investimento deve ter no máximo 2000 caracteres.")
    private String description;

    @NotNull(message = "O valor estimado é obrigatório.")
    @DecimalMin(value = "0.00", inclusive = true, message = "O valor estimado deve ser maior ou igual a zero.")
    private BigDecimal estimatedValue;

    @NotNull(message = "A prioridade do investimento é obrigatória.")
    private InvestmentPriority priority;

    private LocalDate expectedDate;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String notes;
}
