package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.CommissionPayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CommissionPaymentRepository extends JpaRepository<CommissionPayment, Long> {

    @EntityGraph(attributePaths = {"barber"})
    List<CommissionPayment> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"barber"})
    List<CommissionPayment> findByBarberIdOrderByCreatedAtDesc(Long barberId);

    @EntityGraph(attributePaths = {"barber"})
    Optional<CommissionPayment> findByBarberIdAndPeriodStartAndPeriodEnd(
            Long barberId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    @EntityGraph(attributePaths = {"barber"})
    Optional<CommissionPayment> findByBarberIdAndPeriodStartAndPeriodEndAndIdNot(
            Long barberId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Long id
    );

    @Override
    @EntityGraph(attributePaths = {"barber"})
    Optional<CommissionPayment> findById(Long id);
}
