package br.com.studiohenriquecortes.controller;

import br.com.studiohenriquecortes.dto.ClientManagementResponse;
import br.com.studiohenriquecortes.service.ClientManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientManagementService clientManagementService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientManagementResponse>> findAll(
            @RequestParam(required = false) String month
    ) {
        return ResponseEntity.ok(clientManagementService.findAll(month));
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientManagementResponse> block(@PathVariable Long id) {
        return ResponseEntity.ok(clientManagementService.block(id));
    }

    @PatchMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientManagementResponse> unblock(@PathVariable Long id) {
        return ResponseEntity.ok(clientManagementService.unblock(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientManagementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
