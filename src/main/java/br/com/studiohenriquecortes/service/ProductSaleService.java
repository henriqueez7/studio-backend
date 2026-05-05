package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.ProductSaleRequest;
import br.com.studiohenriquecortes.dto.ProductSaleResponse;
import br.com.studiohenriquecortes.entity.Product;
import br.com.studiohenriquecortes.entity.ProductSale;
import br.com.studiohenriquecortes.entity.StockMovement;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.StockMovementType;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.ProductRepository;
import br.com.studiohenriquecortes.repository.ProductSaleRepository;
import br.com.studiohenriquecortes.repository.StockMovementRepository;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSaleService {

    private final ProductSaleRepository productSaleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public ProductSaleResponse create(ProductSaleRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Nao e permitido vender um produto inativo.");
        }

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new BusinessException("Estoque insuficiente para realizar a venda.");
        }

        User seller = findSellerIfProvided(request.getSellerId());
        BigDecimal unitPrice = product.getSalePrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        ProductSale productSale = ProductSale.builder()
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .seller(seller)
                .build();

        productSaleRepository.save(productSale);

        StockMovement stockMovement = StockMovement.builder()
                .product(product)
                .type(StockMovementType.EXIT)
                .quantity(request.getQuantity())
                .reason(buildStockMovementReason(productSale))
                .build();

        stockMovementRepository.save(stockMovement);

        return mapToResponse(productSale);
    }

    @Transactional(readOnly = true)
    public List<ProductSaleResponse> findAll() {
        return productSaleRepository.findAllByOrderBySaleDateDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductSaleResponse findById(Long id) {
        ProductSale productSale = productSaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda de produto nao encontrada"));

        return mapToResponse(productSale);
    }

    private User findSellerIfProvided(Long sellerId) {
        if (sellerId == null) {
            return null;
        }

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor nao encontrado"));

        if (!Boolean.TRUE.equals(seller.getActive())) {
            throw new BusinessException("O vendedor informado esta inativo.");
        }

        if (seller.getRole() != Role.ADMIN && seller.getRole() != Role.BARBEIRO) {
            throw new BusinessException("O vendedor informado nao possui perfil permitido para registrar vendas.");
        }

        return seller;
    }

    private String buildStockMovementReason(ProductSale productSale) {
        String baseReason = "Saida por venda do produto " + productSale.getProduct().getName()
                + " (venda ID " + productSale.getId() + ")";

        if (productSale.getSeller() == null) {
            return baseReason;
        }

        return baseReason + " por " + productSale.getSeller().getName();
    }

    private ProductSaleResponse mapToResponse(ProductSale productSale) {
        return ProductSaleResponse.builder()
                .id(productSale.getId())
                .productId(productSale.getProduct().getId())
                .productName(productSale.getProduct().getName())
                .quantity(productSale.getQuantity())
                .unitPrice(productSale.getUnitPrice())
                .totalPrice(productSale.getTotalPrice())
                .saleDate(productSale.getSaleDate())
                .sellerId(productSale.getSeller() != null ? productSale.getSeller().getId() : null)
                .sellerName(productSale.getSeller() != null ? productSale.getSeller().getName() : null)
                .createdAt(productSale.getCreatedAt())
                .build();
    }
}
