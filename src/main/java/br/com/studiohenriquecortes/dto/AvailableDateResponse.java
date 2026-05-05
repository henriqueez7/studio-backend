package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AvailableDateResponse {

    private LocalDate date;
}
