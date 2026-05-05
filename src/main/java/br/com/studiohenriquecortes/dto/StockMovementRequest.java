package br.com.studiohenriquecortes.dto;

import br.com.studiohenriquecortes.enums.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementRequest {

    @NotNull(message = "O produto é obrigatório.")
    private Long productId;

    @NotNull(message = "O tipo de movimentação é obrigatório.")
    private StockMovementType type;

    @NotNull(message = "A quantidade é obrigatória.")
    @Min(value = 1, message = "A quantidade deve ser maior que zero.")
    private Integer quantity;

    @NotBlank(message = "O motivo da movimentação é obrigatório.")
    private String reason;
}
