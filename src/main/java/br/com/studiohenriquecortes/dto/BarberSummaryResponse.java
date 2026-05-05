package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BarberSummaryResponse {

    private Long id;
    private String name;
    private String email;
}
