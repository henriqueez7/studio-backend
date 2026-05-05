package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.BarberCreateRequest;
import br.com.studiohenriquecortes.dto.BarberResponse;
import br.com.studiohenriquecortes.dto.BarberSummaryResponse;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.exception.ResourceNotFoundException;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public BarberResponse create(BarberCreateRequest request) {
        String normalizedEmail = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String normalizedPhone = request.getPhone() == null ? null : request.getPhone().trim();

        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Ja existe um usuario do sistema cadastrado com este email.");
        }

        if (normalizedPhone != null && !normalizedPhone.isBlank() && userRepository.existsByPhone(normalizedPhone)) {
            throw new BusinessException("Ja existe um usuario do sistema cadastrado com este telefone.");
        }

        User barber = User.builder()
                .name(request.getName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(normalizedPhone == null || normalizedPhone.isBlank() ? null : normalizedPhone)
                .role(Role.BARBEIRO)
                .commissionPercentage(request.getCommissionPercentage())
                .active(true)
                .build();

        userRepository.save(barber);
        emailNotificationService.sendBarberWelcome(barber, request.getPassword());

        return mapToBarberResponse(barber);
    }

    @Transactional(readOnly = true)
    public List<BarberResponse> findAll() {
        return userRepository.findByRoleAndActiveTrueOrderByNameAsc(Role.BARBEIRO)
                .stream()
                .map(this::mapToBarberResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BarberSummaryResponse> findAvailableBarbers() {
        return userRepository.findByRoleAndActiveTrueOrderByNameAsc(Role.BARBEIRO)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BarberResponse deactivate(Long id) {
        User barber = userRepository.findById(id)
                .filter(user -> user.getRole() == Role.BARBEIRO)
                .orElseThrow(() -> new ResourceNotFoundException("Barbeiro nao encontrado."));

        barber.setActive(false);
        barber.setEmail(String.format("deleted-barber-%d-%d@inactive.local", barber.getId(), System.currentTimeMillis()));
        barber.setPhone(null);
        userRepository.save(barber);

        return mapToBarberResponse(barber);
    }

    private BarberSummaryResponse toResponse(User barber) {
        return BarberSummaryResponse.builder()
                .id(barber.getId())
                .name(barber.getName())
                .email(barber.getEmail())
                .build();
    }

    private BarberResponse mapToBarberResponse(User barber) {
        return BarberResponse.builder()
                .id(barber.getId())
                .name(barber.getName())
                .email(barber.getEmail())
                .phone(barber.getPhone())
                .role(barber.getRole().name())
                .commissionPercentage(barber.getCommissionPercentage())
                .active(barber.getActive())
                .build();
    }
}
