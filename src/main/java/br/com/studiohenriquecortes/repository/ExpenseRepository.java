package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
