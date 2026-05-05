package br.com.studiohenriquecortes.dto;

import br.com.studiohenriquecortes.enums.InvestmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvestmentStatusUpdateRequest {

    @NotNull(message = "O status do investimento é obrigatório.")
    private InvestmentStatus status;
}
