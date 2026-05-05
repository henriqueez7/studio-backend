package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppointmentPersonResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Boolean active;
}
