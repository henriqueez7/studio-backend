package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AppointmentServiceItemResponse {

    private Long id;
    private String name;
    private Long serviceId;
    private String serviceName;
    private Integer durationInMinutes;
    private BigDecimal price;
    private Integer position;
}
