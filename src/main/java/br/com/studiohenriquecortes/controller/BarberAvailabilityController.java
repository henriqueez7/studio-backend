package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.BarberAvailabilityRequest;
import br.com.studiohenriquecortes.dto.BarberAvailabilityResponse;
import br.com.studiohenriquecortes.service.BarberAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/barber-availability")
@RequiredArgsConstructor
public class BarberAvailabilityController {

    private final BarberAvailabilityService barberAvailabilityService;

    @GetMapping("/{barberId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<List<BarberAvailabilityResponse>> findByBarber(
            @PathVariable Long barberId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(barberAvailabilityService.findByBarber(barberId, authentication.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<BarberAvailabilityResponse> create(
            @Valid @RequestBody BarberAvailabilityRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(barberAvailabilityService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<BarberAvailabilityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BarberAvailabilityRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(barberAvailabilityService.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        barberAvailabilityService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
