package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.ProductSale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductSaleRepository extends JpaRepository<ProductSale, Long> {

    @EntityGraph(attributePaths = {"product", "seller"})
    List<ProductSale> findAllByOrderBySaleDateDesc();

    @Override
    @EntityGraph(attributePaths = {"product", "seller"})
    Optional<ProductSale> findById(Long id);
}
