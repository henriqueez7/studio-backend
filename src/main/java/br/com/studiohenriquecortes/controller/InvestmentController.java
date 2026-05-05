package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.InvestmentRequest;
import br.com.studiohenriquecortes.dto.InvestmentResponse;
import br.com.studiohenriquecortes.dto.InvestmentStatusUpdateRequest;
import br.com.studiohenriquecortes.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvestmentResponse> create(@Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.ok(investmentService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvestmentResponse>> findAll() {
        return ResponseEntity.ok(investmentService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvestmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(investmentService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvestmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody InvestmentRequest request
    ) {
        return ResponseEntity.ok(investmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        investmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvestmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody InvestmentStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(investmentService.updateStatus(id, request));
    }
}
