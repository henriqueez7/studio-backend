package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AppointmentNotificationSettingsResponse {

    private Boolean enabled;
    private String messageTemplate;
    private String address;
    private Boolean whatsappConfigured;
    private List<String> supportedVariables;
}
