package br.com.studiohenriquecortes.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockAlertResponse {

    private Long productId;
    private String productName;
    private String category;
    private Integer stockQuantity;
    private Integer minimumStock;
}
