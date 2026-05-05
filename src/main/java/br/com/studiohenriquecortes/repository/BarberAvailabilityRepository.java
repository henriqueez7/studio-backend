package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.BarberAvailability;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface BarberAvailabilityRepository extends JpaRepository<BarberAvailability, Long> {

    @EntityGraph(attributePaths = {"barber"})
    List<BarberAvailability> findByBarberIdOrderByDayOfWeekAsc(Long barberId);

    @EntityGraph(attributePaths = {"barber"})
    Optional<BarberAvailability> findByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek);

    boolean existsByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek);

    @Override
    @EntityGraph(attributePaths = {"barber"})
    Optional<BarberAvailability> findById(Long id);
}
