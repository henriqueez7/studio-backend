package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.StockMovementRequest;
import br.com.studiohenriquecortes.dto.StockMovementResponse;
import br.com.studiohenriquecortes.entity.Product;
import br.com.studiohenriquecortes.entity.StockMovement;
import br.com.studiohenriquecortes.enums.StockMovementType;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.ProductRepository;
import br.com.studiohenriquecortes.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Transactional
    public StockMovementResponse create(StockMovementRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));

        Integer currentStockQuantity = calculateCurrentStockQuantity(
                request.getType(),
                request.getQuantity(),
                product.getStockQuantity()
        );

        product.setStockQuantity(currentStockQuantity);
        productRepository.save(product);

        StockMovement stockMovement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .build();

        stockMovementRepository.save(stockMovement);

        return mapToResponse(stockMovement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findAll() {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Produto nao encontrado");
        }

        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Integer calculateCurrentStockQuantity(
            StockMovementType type,
            Integer quantity,
            Integer currentStockQuantity
    ) {
        return switch (type) {
            case ENTRY -> currentStockQuantity + quantity;
            case EXIT -> calculateExitStock(currentStockQuantity, quantity);
            case ADJUSTMENT -> quantity;
        };
    }

    private Integer calculateExitStock(Integer currentStockQuantity, Integer quantity) {
        if (quantity > currentStockQuantity) {
            throw new BusinessException("A saida nao pode ser maior que o estoque disponivel.");
        }

        return currentStockQuantity - quantity;
    }

    private StockMovementResponse mapToResponse(StockMovement stockMovement) {
        return StockMovementResponse.builder()
                .id(stockMovement.getId())
                .productId(stockMovement.getProduct().getId())
                .productName(stockMovement.getProduct().getName())
                .type(stockMovement.getType())
                .quantity(stockMovement.getQuantity())
                .reason(stockMovement.getReason())
                .createdAt(stockMovement.getCreatedAt())
                .build();
    }
}
