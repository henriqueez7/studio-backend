package br.com.studiohenriquecortes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentNotificationSettingsRequest {

    private Boolean enabled;

    @NotBlank(message = "A mensagem automatica e obrigatoria.")
    private String messageTemplate;

    private String address;
}
