package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CommissionCalculationResponse {

    private Long barberId;
    private String barberName;
    private BigDecimal commissionPercentage;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal grossServiceAmount;
    private BigDecimal commissionAmount;
    private Integer completedAppointments;
}
