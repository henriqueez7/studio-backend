package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.ClientManagementResponse;
import br.com.studiohenriquecortes.entity.Appointment;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.AppointmentStatus;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.AppointmentRepository;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientManagementService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<ClientManagementResponse> findAll(String month) {
        YearMonth selectedMonth = resolveMonth(month);
        List<User> clients = userRepository.findByRoleOrderByNameAsc(Role.CLIENTE);
        List<Appointment> appointments = appointmentRepository.findAllDetailed();

        Map<Long, ClientMetrics> totalMetrics = new HashMap<>();
        Map<Long, ClientMetrics> monthlyMetrics = new HashMap<>();

        for (Appointment appointment : appointments) {
            if (appointment.getClient() == null || appointment.getClient().getId() == null) {
                continue;
            }

            if (appointment.getStatus() == AppointmentStatus.CANCELADO) {
                continue;
            }

            Long clientId = appointment.getClient().getId();
            BigDecimal value = appointment.getTotalPrice() != null ? appointment.getTotalPrice() : BigDecimal.ZERO;
            LocalDate appointmentDate = appointment.getAppointmentDate();

            totalMetrics.computeIfAbsent(clientId, ignored -> new ClientMetrics())
                    .register(value, appointmentDate);

            if (appointmentDate != null && YearMonth.from(appointmentDate).equals(selectedMonth)) {
                monthlyMetrics.computeIfAbsent(clientId, ignored -> new ClientMetrics())
                        .register(value, appointmentDate);
            }
        }

        return clients.stream()
                .map(client -> toResponse(client, totalMetrics.get(client.getId()), monthlyMetrics.get(client.getId())))
                .toList();
    }

    @Transactional
    public ClientManagementResponse block(Long id) {
        User client = loadClient(id);
        client.setActive(false);
        userRepository.save(client);
        return toResponse(client, null, null);
    }

    @Transactional
    public ClientManagementResponse unblock(Long id) {
        User client = loadClient(id);
        client.setActive(true);
        userRepository.save(client);
        return toResponse(client, null, null);
    }

    @Transactional
    public void delete(Long id) {
        User client = loadClient(id);

        if (!appointmentRepository.findDetailedByClientId(id).isEmpty()) {
            throw new BusinessException("Este cliente possui agendamentos vinculados e nao pode ser excluido.");
        }

        userRepository.delete(client);
    }

    private User loadClient(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == Role.CLIENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado."));
    }

    private YearMonth resolveMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }

        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new BusinessException("Formato de mes invalido. Use AAAA-MM.");
        }
    }

    private ClientManagementResponse toResponse(User client, ClientMetrics total, ClientMetrics monthly) {
        ClientMetrics totalMetrics = total != null ? total : new ClientMetrics();
        ClientMetrics monthlyMetrics = monthly != null ? monthly : new ClientMetrics();

        return ClientManagementResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .active(client.getActive())
                .monthlySpent(monthlyMetrics.totalSpent)
                .totalSpent(totalMetrics.totalSpent)
                .appointmentsCount(monthlyMetrics.count)
                .lastAppointmentDate(totalMetrics.lastAppointmentDate)
                .build();
    }

    private static class ClientMetrics {
        private BigDecimal totalSpent = BigDecimal.ZERO;
        private Long count = 0L;
        private LocalDate lastAppointmentDate;

        private void register(BigDecimal value, LocalDate appointmentDate) {
            totalSpent = totalSpent.add(value != null ? value : BigDecimal.ZERO);
            count++;

            if (appointmentDate != null
                    && (lastAppointmentDate == null || appointmentDate.isAfter(lastAppointmentDate))) {
                lastAppointmentDate = appointmentDate;
            }
        }
    }
}
