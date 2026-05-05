package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {
}
