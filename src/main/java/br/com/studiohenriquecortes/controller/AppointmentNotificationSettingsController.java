package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.AppointmentNotificationSettingsRequest;
import br.com.studiohenriquecortes.dto.AppointmentNotificationSettingsResponse;
import br.com.studiohenriquecortes.service.AppointmentNotificationSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/appointment-notifications")
@RequiredArgsConstructor
public class AppointmentNotificationSettingsController {

    private final AppointmentNotificationSettingsService appointmentNotificationSettingsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentNotificationSettingsResponse> getSettings() {
        return ResponseEntity.ok(appointmentNotificationSettingsService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentNotificationSettingsResponse> update(
            @Valid @RequestBody AppointmentNotificationSettingsRequest request
    ) {
        return ResponseEntity.ok(appointmentNotificationSettingsService.update(request));
    }
}
