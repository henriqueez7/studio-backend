package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BarberResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private BigDecimal commissionPercentage;
    private Boolean active;
}
