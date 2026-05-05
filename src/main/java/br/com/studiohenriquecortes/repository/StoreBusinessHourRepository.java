package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.StoreBusinessHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface StoreBusinessHourRepository extends JpaRepository<StoreBusinessHour, Long> {

    List<StoreBusinessHour> findAllByOrderByDayOfWeekAsc();

    Optional<StoreBusinessHour> findByDayOfWeek(DayOfWeek dayOfWeek);
}
