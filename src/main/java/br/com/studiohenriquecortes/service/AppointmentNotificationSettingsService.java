package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.AppointmentNotificationSettingsRequest;
import br.com.studiohenriquecortes.dto.AppointmentNotificationSettingsResponse;
import br.com.studiohenriquecortes.entity.AppointmentNotificationSettings;
import br.com.studiohenriquecortes.repository.AppointmentNotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentNotificationSettingsService {

    public static final List<String> SUPPORTED_VARIABLES = List.of(
            "{{clientName}}",
            "{{barberName}}",
            "{{serviceNames}}",
            "{{date}}",
            "{{time}}",
            "{{address}}",
            "{{price}}"
    );

    private static final String DEFAULT_TEMPLATE = """
            Ola, {{clientName}}! ✨ Seu horario foi confirmado com sucesso.

            📅 Data: {{date}}
            ⏰ Horario: {{time}}
            💈 Servicos: {{serviceNames}}
            ✂️ Barbeiro: {{barberName}}
            📍 Endereco: {{address}}
            💵 Valor do atendimento: {{price}}

            Pedimos que chegue com 10 minutos de antecedencia para uma experiencia mais tranquila.
            Em caso de atraso sem aviso previo, podera ser cobrada uma taxa de 50% sobre o valor do atendimento.

            Se precisar de qualquer ajuste, estamos a disposicao. 🤝
            """;

    private final AppointmentNotificationSettingsRepository repository;
    private final WhatsappNotificationService whatsappNotificationService;

    public AppointmentNotificationSettingsResponse getSettings() {
        return toResponse(loadOrCreateDefault());
    }

    @Transactional
    public AppointmentNotificationSettingsResponse update(AppointmentNotificationSettingsRequest request) {
        AppointmentNotificationSettings settings = loadOrCreateDefault();
        settings.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        settings.setMessageTemplate(normalizeTemplate(request.getMessageTemplate()));
        settings.setAddress(normalizeAddress(request.getAddress()));

        repository.save(settings);

        return toResponse(settings);
    }

    @Transactional
    public AppointmentNotificationSettings loadOrCreateDefault() {
        return repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> repository.save(AppointmentNotificationSettings.builder()
                        .enabled(false)
                        .messageTemplate(DEFAULT_TEMPLATE)
                        .address("")
                        .build()));
    }

    private AppointmentNotificationSettingsResponse toResponse(AppointmentNotificationSettings settings) {
        return AppointmentNotificationSettingsResponse.builder()
                .enabled(settings.getEnabled())
                .messageTemplate(settings.getMessageTemplate())
                .address(settings.getAddress())
                .whatsappConfigured(whatsappNotificationService.isConfigured())
                .supportedVariables(SUPPORTED_VARIABLES)
                .build();
    }

    private String normalizeTemplate(String template) {
        String normalized = template == null ? "" : template.trim();
        return normalized.isEmpty() ? DEFAULT_TEMPLATE : normalized;
    }

    private String normalizeAddress(String address) {
        return address == null ? "" : address.trim();
    }
}
