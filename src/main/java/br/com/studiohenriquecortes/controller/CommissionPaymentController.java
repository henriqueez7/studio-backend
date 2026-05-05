package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.CommissionCalculationResponse;
import br.com.studiohenriquecortes.dto.CommissionPaymentRequest;
import br.com.studiohenriquecortes.dto.CommissionPaymentResponse;
import br.com.studiohenriquecortes.service.CommissionPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/commissions")
@RequiredArgsConstructor
public class CommissionPaymentController {

    private final CommissionPaymentService commissionPaymentService;

    @PostMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommissionPaymentResponse> createPayment(
            @Valid @RequestBody CommissionPaymentRequest request
    ) {
        return ResponseEntity.ok(commissionPaymentService.createOrUpdate(request));
    }

    @PutMapping("/payments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommissionPaymentResponse> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody CommissionPaymentRequest request
    ) {
        return ResponseEntity.ok(commissionPaymentService.update(id, request));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommissionPaymentResponse>> findAllPayments() {
        return ResponseEntity.ok(commissionPaymentService.findAll());
    }

    @GetMapping("/payments/barber/{barberId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<List<CommissionPaymentResponse>> findPaymentsByBarber(
            @PathVariable Long barberId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(commissionPaymentService.findByBarber(barberId, authentication.getName()));
    }

    @GetMapping("/calculate/barber/{barberId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<CommissionCalculationResponse> calculateCommission(
            @PathVariable Long barberId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Authentication authentication
    ) {
        return ResponseEntity.ok(commissionPaymentService.calculate(barberId, start, end, authentication.getName()));
    }

    @PutMapping("/payments/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommissionPaymentResponse> pay(@PathVariable Long id) {
        return ResponseEntity.ok(commissionPaymentService.pay(id));
    }

    @DeleteMapping("/payments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commissionPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
