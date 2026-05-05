package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.BarberCreateRequest;
import br.com.studiohenriquecortes.dto.BarberResponse;
import br.com.studiohenriquecortes.dto.BarberSummaryResponse;
import br.com.studiohenriquecortes.service.BarberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberService barberService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BarberResponse> create(@Valid @RequestBody BarberCreateRequest request) {
        return ResponseEntity.ok(barberService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BarberResponse>> findAll() {
        return ResponseEntity.ok(barberService.findAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BarberResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(barberService.deactivate(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<BarberSummaryResponse>> findAvailableBarbers() {
        return ResponseEntity.ok(barberService.findAvailableBarbers());
    }
}
