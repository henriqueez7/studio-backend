package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FinancialSummaryResponse {

    private BigDecimal revenueFromServices;
    private BigDecimal revenueFromProductSales;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal totalPaidCommissions;
    private BigDecimal netProfit;
}
