package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.Appointment;
import br.com.studiohenriquecortes.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
            select distinct a
            from Appointment a
            left join fetch a.client
            left join fetch a.barber
            left join fetch a.items i
            left join fetch i.service
            where a.barber.id = :barberId
              and a.appointmentDate = :appointmentDate
            """)
    List<Appointment> findDetailedByBarberIdAndAppointmentDate(Long barberId, LocalDate appointmentDate);

    @Query("""
            select distinct a
            from Appointment a
            left join fetch a.client
            left join fetch a.barber
            left join fetch a.items i
            left join fetch i.service
            where a.barber.id = :barberId
            """)
    List<Appointment> findDetailedByBarberId(Long barberId);

    @Query("""
            select distinct a
            from Appointment a
            left join fetch a.client
            left join fetch a.barber
            left join fetch a.items i
            left join fetch i.service
            where a.client.id = :clientId
            """)
    List<Appointment> findDetailedByClientId(Long clientId);

    @Query("""
            select distinct a
            from Appointment a
            left join fetch a.client
            left join fetch a.barber
            left join fetch a.items i
            left join fetch i.service
            where a.barber.id = :barberId
              and a.appointmentDate between :startDate and :endDate
              and a.status = :status
            """)
    List<Appointment> findDetailedByBarberIdAndAppointmentDateBetweenAndStatus(
            Long barberId,
            LocalDate startDate,
            LocalDate endDate,
            AppointmentStatus status
    );

    @Query("""
            select distinct a
            from Appointment a
            left join fetch a.client
            left join fetch a.barber
            left join fetch a.items i
            left join fetch i.service
            """)
    List<Appointment> findAllDetailed();

    @Query("""
            select distinct a
            from Appointment a
            left join fetch a.client
            left join fetch a.barber
            left join fetch a.items i
            left join fetch i.service
            where a.id = :id
            """)
    Optional<Appointment> findDetailedById(Long id);
}
