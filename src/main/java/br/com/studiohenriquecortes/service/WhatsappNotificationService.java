package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.entity.Appointment;
import br.com.studiohenriquecortes.entity.AppointmentNotificationSettings;
import br.com.studiohenriquecortes.entity.AppointmentServiceItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsappNotificationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${app.whatsapp.api-url:https://graph.facebook.com/v22.0}")
    private String apiUrl;

    @Value("${app.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${app.whatsapp.access-token:}")
    private String accessToken;

    @Value("${app.whatsapp.default-country-code:55}")
    private String defaultCountryCode;

    private final ObjectMapper objectMapper;

    public boolean isConfigured() {
        return hasText(phoneNumberId) && hasText(accessToken);
    }

    public void sendConfirmation(Appointment appointment, AppointmentNotificationSettings settings) {
        if (appointment == null || settings == null || !Boolean.TRUE.equals(settings.getEnabled())) {
            return;
        }

        String phone = normalizePhone(appointment.getClient() != null ? appointment.getClient().getPhone() : null);
        if (!hasText(phone)) {
            log.info("WhatsApp nao enviado para agendamento {}: cliente sem telefone.", appointment.getId());
            return;
        }

        if (!isConfigured()) {
            log.info("WhatsApp nao enviado para agendamento {}: integracao nao configurada.", appointment.getId());
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "messaging_product", "whatsapp",
                    "to", phone,
                    "type", "text",
                    "text", Map.of("body", buildMessage(appointment, settings))
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%s/%s/messages", apiUrl, phoneNumberId)))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 400) {
                log.warn("Falha ao enviar WhatsApp do agendamento {}. Status: {}. Resposta: {}",
                        appointment.getId(), response.statusCode(), response.body());
            }
        } catch (Exception exception) {
            log.warn("Falha ao enviar WhatsApp do agendamento {}.", appointment.getId(), exception);
        }
    }

    private String buildMessage(Appointment appointment, AppointmentNotificationSettings settings) {
        String address = hasText(settings.getAddress()) ? settings.getAddress().trim() : "Endereco nao informado";
        String template = hasText(settings.getMessageTemplate()) ? settings.getMessageTemplate() : "";

        Map<String, String> replacements = Map.of(
                "{{clientName}}", safeValue(appointment.getClient() != null ? appointment.getClient().getName() : null),
                "{{barberName}}", safeValue(appointment.getBarber() != null ? appointment.getBarber().getName() : null),
                "{{serviceNames}}", safeValue(extractServiceNames(appointment)),
                "{{date}}", appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().format(DATE_FORMATTER) : "-",
                "{{time}}", appointment.getStartTime() != null ? appointment.getStartTime().format(TIME_FORMATTER) : "-",
                "{{address}}", address,
                "{{price}}", formatPrice(appointment.getTotalPrice())
        );

        String message = template;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        return message.trim();
    }

    private String extractServiceNames(Appointment appointment) {
        if (appointment.getItems() == null || appointment.getItems().isEmpty()) {
            return "Servico nao informado";
        }

        Set<String> serviceNames = appointment.getItems().stream()
                .map(AppointmentServiceItem::getServiceNameSnapshot)
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return serviceNames.isEmpty() ? "Servico nao informado" : String.join(", ", serviceNames);
    }

    private String formatPrice(BigDecimal price) {
        BigDecimal normalized = price == null ? BigDecimal.ZERO : price;
        return "R$ " + normalized.setScale(2, java.math.RoundingMode.HALF_UP)
                .toString()
                .replace(".", ",");
    }

    private String normalizePhone(String phone) {
        if (!hasText(phone)) {
            return null;
        }

        String digits = Normalizer.normalize(phone, Normalizer.Form.NFD)
                .replaceAll("[^\\d]", "");

        if (!hasText(digits)) {
            return null;
        }

        if (hasText(defaultCountryCode) && !digits.startsWith(defaultCountryCode)) {
            digits = defaultCountryCode + digits;
        }

        return digits;
    }

    private String safeValue(String value) {
        return hasText(value) ? value.trim() : "-";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
