package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "O nome do produto é obrigatório.")
    @Size(max = 120, message = "O nome do produto deve ter no máximo 120 caracteres.")
    private String name;

    @Size(max = 1000, message = "A descrição do produto deve ter no máximo 1000 caracteres.")
    private String description;

    @Size(max = 80, message = "A categoria do produto deve ter no máximo 80 caracteres.")
    private String category;

    @NotNull(message = "O preço de compra é obrigatório.")
    @DecimalMin(value = "0.00", inclusive = true, message = "O preço de compra não pode ser negativo.")
    private BigDecimal purchasePrice;

    @NotNull(message = "O preço de venda é obrigatório.")
    @DecimalMin(value = "0.01", inclusive = true, message = "O preço de venda deve ser maior que zero.")
    private BigDecimal salePrice;

    @NotNull(message = "A quantidade em estoque é obrigatória.")
    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa.")
    private Integer stockQuantity;

    @NotNull(message = "O estoque mínimo é obrigatório.")
    @Min(value = 0, message = "O estoque mínimo não pode ser negativo.")
    private Integer minimumStock;
}
