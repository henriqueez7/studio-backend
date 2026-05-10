package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.dto.AuthResponse;
import br.com.studiohenriquecortes.dto.LoginRequest;
import br.com.studiohenriquecortes.dto.RegisterRequest;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.exception.BusinessException;
import br.com.studiohenriquecortes.repository.UserRepository;
import br.com.studiohenriquecortes.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailNotificationService emailNotificationService;

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String normalizedPhone = request.getPhone() == null ? null : request.getPhone().trim();

        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Ja existe um usuario do sistema cadastrado com este email.");
        }

        if (normalizedPhone != null && !normalizedPhone.isBlank() && userRepository.existsByPhone(normalizedPhone)) {
            throw new BusinessException("Ja existe um usuario do sistema cadastrado com este telefone.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(normalizedPhone == null || normalizedPhone.isBlank() ? null : normalizedPhone)
                .role(Role.CLIENTE)
                .active(true)
                .build();

        userRepository.save(user);
        emailNotificationService.sendClientWelcome(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticate(request);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario autenticado nao encontrado."));

        return buildAuthResponse(user);
    }

    private void authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciais invalidas."));

        if (!Boolean.TRUE.equals(user.getActive())
                || user.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Credenciais invalidas.");
        }
    }

    public AuthResponse loginWithGoogle(String email, String name) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Nao foi possivel obter o email da conta Google.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .map(existing -> updateGoogleUser(existing, name))
                .orElseGet(() -> createGoogleUser(normalizedEmail, name));

        return buildAuthResponse(user);
    }

    private User updateGoogleUser(User existingUser, String name) {
        if (name != null && !name.isBlank()) {
            existingUser.setName(name);
        }

        if (!Boolean.TRUE.equals(existingUser.getActive())) {
            existingUser.setActive(true);
        }

        return userRepository.save(existingUser);
    }

    private User createGoogleUser(String email, String name) {
        User user = User.builder()
                .name((name == null || name.isBlank()) ? "Cliente Google" : name)
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.CLIENTE)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
