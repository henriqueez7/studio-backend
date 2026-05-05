package br.com.studiohenriquecortes.dto;

import br.com.studiohenriquecortes.enums.InvestmentPriority;
import br.com.studiohenriquecortes.enums.InvestmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class InvestmentResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal estimatedValue;
    private InvestmentPriority priority;
    private InvestmentStatus status;
    private LocalDate expectedDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
