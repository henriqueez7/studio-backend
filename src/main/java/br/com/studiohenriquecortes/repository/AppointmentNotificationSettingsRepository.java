package br.com.studiohenriquecortes.repository;

import br.com.studiohenriquecortes.entity.AppointmentNotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentNotificationSettingsRepository extends JpaRepository<AppointmentNotificationSettings, Long> {
}
