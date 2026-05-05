package br.com.studiohenriquecortes.security;

import br.com.studiohenriquecortes.dto.AuthResponse;
import br.com.studiohenriquecortes.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        AuthResponse authResponse = authService.loginWithGoogle(email, name);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/auth/google/callback")
                .queryParam("token", authResponse.getToken())
                .queryParam("id", authResponse.getId())
                .queryParam("name", authResponse.getName())
                .queryParam("email", authResponse.getEmail())
                .queryParam("role", authResponse.getRole())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
