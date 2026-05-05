package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponse {

    private Long totalClients;
    private Long totalBarbers;
    private Long totalServices;
    private Long totalActiveProducts;
    private Long totalAppointments;
}
