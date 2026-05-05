package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.StockMovementRequest;
import br.com.studiohenriquecortes.dto.StockMovementResponse;
import br.com.studiohenriquecortes.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<StockMovementResponse> create(@Valid @RequestBody StockMovementRequest request) {
        return ResponseEntity.ok(stockMovementService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<List<StockMovementResponse>> findAll() {
        return ResponseEntity.ok(stockMovementService.findAll());
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<List<StockMovementResponse>> findByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementService.findByProduct(productId));
    }
}
