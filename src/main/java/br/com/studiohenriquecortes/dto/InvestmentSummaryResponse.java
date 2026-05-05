package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InvestmentSummaryResponse {

    private Long plannedCount;
    private Long inProgressCount;
    private Long completedCount;
    private Long canceledCount;
    private BigDecimal totalPlannedValue;
    private BigDecimal totalCompletedValue;
}
