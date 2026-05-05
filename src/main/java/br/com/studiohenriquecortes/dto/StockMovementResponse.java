package br.com.studiohenriquecortes.dto;

import br.com.studiohenriquecortes.enums.StockMovementType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StockMovementResponse {

    private Long id;
    private Long productId;
    private String productName;
    private StockMovementType type;
    private Integer quantity;
    private String reason;
    private LocalDateTime createdAt;
}
