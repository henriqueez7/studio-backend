package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CommissionSummaryResponse {

    private BigDecimal totalPaidCommissions;
    private BigDecimal totalPendingCommissions;
    private List<BarberCommissionSummaryResponse> commissionsByBarber;
}
