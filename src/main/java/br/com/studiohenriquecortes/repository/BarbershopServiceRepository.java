package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.BarbershopService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarbershopServiceRepository extends JpaRepository<BarbershopService, Long> {

    List<BarbershopService> findByActiveTrue();

    List<BarbershopService> findByIdInAndActiveTrue(List<Long> ids);

    boolean existsByNameIgnoreCase(String name);
}
