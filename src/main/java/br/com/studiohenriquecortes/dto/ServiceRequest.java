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
public class ServiceRequest {

    @NotBlank(message = "O nome do servico e obrigatorio.")
    @Size(max = 120, message = "O nome do servico deve ter no maximo 120 caracteres.")
    private String name;

    @Size(max = 1000, message = "A descricao do servico deve ter no maximo 1000 caracteres.")
    private String description;

    @NotNull(message = "O preco do servico e obrigatorio.")
    @DecimalMin(value = "0.01", message = "O preco do servico deve ser maior que zero.")
    private BigDecimal price;

    @NotNull(message = "A duracao do servico e obrigatoria.")
    @Min(value = 1, message = "A duracao do servico deve ser maior que zero.")
    private Integer durationInMinutes;
}
