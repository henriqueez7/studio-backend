package br.com.studiohenriquecortes.dto;

import br.com.studiohenriquecortes.enums.CommissionPaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CommissionPaymentResponse {

    private Long id;
    private Long barberId;
    private String barberName;
    private BigDecimal commissionPercentage;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalAmount;
    private CommissionPaymentStatus status;
    private LocalDate paymentDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
