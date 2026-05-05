package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.CommissionSummaryResponse;
import br.com.studiohenriquecortes.dto.DashboardSummaryResponse;
import br.com.studiohenriquecortes.dto.FinancialSummaryResponse;
import br.com.studiohenriquecortes.dto.InvestmentSummaryResponse;
import br.com.studiohenriquecortes.dto.StockAlertResponse;
import br.com.studiohenriquecortes.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/financial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialSummaryResponse> getFinancialSummary() {
        return ResponseEntity.ok(dashboardService.getFinancialSummary());
    }

    @GetMapping("/stock-alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockAlertResponse>> getStockAlerts() {
        return ResponseEntity.ok(dashboardService.getStockAlerts());
    }

    @GetMapping("/commissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommissionSummaryResponse> getCommissionSummary() {
        return ResponseEntity.ok(dashboardService.getCommissionSummary());
    }

    @GetMapping("/investments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvestmentSummaryResponse> getInvestmentSummary() {
        return ResponseEntity.ok(dashboardService.getInvestmentSummary());
    }
}
