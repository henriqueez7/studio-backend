package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.StoreBusinessHourRequest;
import br.com.studiohenriquecortes.dto.StoreBusinessHourResponse;
import br.com.studiohenriquecortes.service.StoreBusinessHourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/store-hours")
@RequiredArgsConstructor
public class StoreBusinessHourController {

    private final StoreBusinessHourService storeBusinessHourService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<List<StoreBusinessHourResponse>> findAll() {
        return ResponseEntity.ok(storeBusinessHourService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreBusinessHourResponse> create(@Valid @RequestBody StoreBusinessHourRequest request) {
        return ResponseEntity.ok(storeBusinessHourService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreBusinessHourResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StoreBusinessHourRequest request
    ) {
        return ResponseEntity.ok(storeBusinessHourService.update(id, request));
    }
}
