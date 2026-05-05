package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.ProductRequest;
import br.com.studiohenriquecortes.dto.ProductResponse;
import br.com.studiohenriquecortes.entity.Product;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .purchasePrice(request.getPurchasePrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getStockQuantity())
                .minimumStock(request.getMinimumStock())
                .active(true)
                .build();

        productRepository.save(product);

        return mapToResponse(product);
    }

    public List<ProductResponse> findAllActive() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        Product product = findProductEntityById(id);
        return mapToResponse(product);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProductEntityById(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setMinimumStock(request.getMinimumStock());

        productRepository.save(product);

        return mapToResponse(product);
    }

    public void deactivate(Long id) {
        Product product = findProductEntityById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .purchasePrice(product.getPurchasePrice())
                .salePrice(product.getSalePrice())
                .stockQuantity(product.getStockQuantity())
                .minimumStock(product.getMinimumStock())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
