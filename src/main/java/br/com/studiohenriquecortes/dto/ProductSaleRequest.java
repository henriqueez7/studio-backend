package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSaleRequest {

    @NotNull(message = "O produto é obrigatório.")
    private Long productId;

    @NotNull(message = "A quantidade é obrigatória.")
    @Min(value = 1, message = "A quantidade deve ser maior que zero.")
    private Integer quantity;

    private Long sellerId;
}
