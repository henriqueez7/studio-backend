package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class AppointmentResponse {

    private Long id;
    private LocalDate appointmentDate;
    private Long clientId;
    private String clientName;
    private Long barberId;
    private String barberName;
    private AppointmentPersonResponse client;
    private AppointmentPersonResponse barber;
    private List<AppointmentServiceItemResponse> services;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalDurationInMinutes;
    private BigDecimal totalPrice;
    private String notes;
    private String status;
}
