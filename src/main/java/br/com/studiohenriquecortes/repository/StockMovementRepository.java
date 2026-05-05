package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.StockMovement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<StockMovement> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"product"})
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
}
