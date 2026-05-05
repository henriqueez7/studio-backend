package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.ProductSaleRequest;
import br.com.studiohenriquecortes.dto.ProductSaleResponse;
import br.com.studiohenriquecortes.service.ProductSaleService;
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
@RequestMapping("/product-sales")
@RequiredArgsConstructor
public class ProductSaleController {

    private final ProductSaleService productSaleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductSaleResponse> create(@Valid @RequestBody ProductSaleRequest request) {
        return ResponseEntity.ok(productSaleService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductSaleResponse>> findAll() {
        return ResponseEntity.ok(productSaleService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductSaleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productSaleService.findById(id));
    }
}
