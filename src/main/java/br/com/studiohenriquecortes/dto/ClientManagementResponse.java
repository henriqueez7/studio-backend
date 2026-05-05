package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ClientManagementResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Boolean active;
    private BigDecimal monthlySpent;
    private BigDecimal totalSpent;
    private Long appointmentsCount;
    private LocalDate lastAppointmentDate;
}
