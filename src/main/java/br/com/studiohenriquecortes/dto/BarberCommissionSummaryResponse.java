package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BarberCommissionSummaryResponse {

    private Long barberId;
    private String barberName;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
}
