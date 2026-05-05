package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.AppointmentRequest;
import br.com.studiohenriquecortes.dto.AppointmentResponse;
import br.com.studiohenriquecortes.dto.AvailableDateResponse;
import br.com.studiohenriquecortes.dto.AvailableTimeResponse;
import br.com.studiohenriquecortes.dto.ScheduleBlockRequest;
import br.com.studiohenriquecortes.dto.ScheduleBlockResponse;
import br.com.studiohenriquecortes.service.AppointmentService;
import br.com.studiohenriquecortes.service.ScheduleBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ScheduleBlockService scheduleBlockService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO','CLIENTE')")
    public ResponseEntity<AppointmentResponse> create(
            @Valid @RequestBody AppointmentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(appointmentService.create(request, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> findAll() {
        return ResponseEntity.ok(appointmentService.findAll());
    }

    @GetMapping("/barber/{barberId}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<List<AppointmentResponse>> findByBarber(
            @PathVariable Long barberId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(appointmentService.findByBarber(barberId, authentication.getName()));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<List<AppointmentResponse>> findByClient(
            @PathVariable Long clientId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(appointmentService.findByClient(clientId, authentication.getName()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<AppointmentResponse>> findMine(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.findByAuthenticatedClient(authentication.getName()));
    }

    @GetMapping("/availability/{barberId}/dates")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO','CLIENTE')")
    public ResponseEntity<List<AvailableDateResponse>> findAvailableDates(
            @PathVariable Long barberId,
            @RequestParam("serviceIds") List<Long> serviceIds
    ) {
        return ResponseEntity.ok(appointmentService.findAvailableDates(barberId, serviceIds));
    }

    @GetMapping("/availability/{barberId}/times")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO','CLIENTE')")
    public ResponseEntity<List<AvailableTimeResponse>> findAvailableTimes(
            @PathVariable Long barberId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("serviceIds") List<Long> serviceIds,
            @RequestParam(value = "appointmentId", required = false) Long appointmentId
    ) {
        return ResponseEntity.ok(appointmentService.findAvailableTimes(barberId, date, serviceIds, appointmentId));
    }

    @GetMapping("/blocks")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<List<ScheduleBlockResponse>> findBlocks(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "barberId", required = false) Long barberId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(scheduleBlockService.findBlocks(date, barberId, authentication.getName()));
    }

    @PostMapping("/blocks")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<ScheduleBlockResponse> createBlock(
            @Valid @RequestBody ScheduleBlockRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(scheduleBlockService.create(request, authentication.getName()));
    }

    @PutMapping("/blocks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<ScheduleBlockResponse> updateBlock(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleBlockRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(scheduleBlockService.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/blocks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable Long id,
            Authentication authentication
    ) {
        scheduleBlockService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(appointmentService.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        appointmentService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<AppointmentResponse> confirm(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(appointmentService.confirm(id, authentication.getName()));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(appointmentService.cancel(id, authentication.getName()));
    }

    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAnyRole('ADMIN','BARBEIRO')")
    public ResponseEntity<AppointmentResponse> finish(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(appointmentService.finish(id, authentication.getName()));
    }
}

