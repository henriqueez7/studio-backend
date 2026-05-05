package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.ScheduleBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, Long> {

    @Query("""
            select sb
            from ScheduleBlock sb
            left join fetch sb.barber
            where sb.blockDate = :blockDate
            order by sb.startTime asc, sb.id asc
            """)
    List<ScheduleBlock> findDetailedByBlockDate(@Param("blockDate") LocalDate blockDate);

    @Query("""
            select sb
            from ScheduleBlock sb
            left join fetch sb.barber
            where sb.barber.id = :barberId
              and sb.blockDate = :blockDate
            order by sb.startTime asc, sb.id asc
            """)
    List<ScheduleBlock> findDetailedByBarberIdAndBlockDate(
            @Param("barberId") Long barberId,
            @Param("blockDate") LocalDate blockDate
    );

    @Query("""
            select sb
            from ScheduleBlock sb
            left join fetch sb.barber
            where sb.id = :id
            """)
    Optional<ScheduleBlock> findDetailedById(@Param("id") Long id);
}
