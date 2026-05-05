package br.com.studiohenriquecortes.service;

import br.com.studiohenriquecortes.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.email.from:}")
    private String from;

    @Value("${app.email.enabled:false}")
    private boolean enabled;

    public void sendBarberWelcome(User barber, String temporaryPassword) {
        String body = """
                Olá, %s!

                Seu acesso ao painel do Studio Henrique Corte foi criado.

                E-mail: %s
                Senha inicial: %s

                Acesse o sistema e, se possível, altere sua senha depois do primeiro acesso.

                Studio Henrique Corte
                """.formatted(
                safe(barber.getName(), "profissional"),
                safe(barber.getEmail(), ""),
                safe(temporaryPassword, "")
        );

        send(barber.getEmail(), "Seu acesso ao Studio Henrique Corte", body);
    }

    public void sendClientWelcome(User client) {
        String body = """
                Olá, %s!

                Seu cadastro no Studio Henrique Corte foi confirmado com sucesso.

                Agora você já pode acessar sua conta, acompanhar seus agendamentos e reservar seus horários.

                Studio Henrique Corte
                """.formatted(safe(client.getName(), "cliente"));

        send(client.getEmail(), "Cadastro confirmado no Studio Henrique Corte", body);
    }

    private void send(String to, String subject, String body) {
        if (!enabled) {
            log.info("E-mail nao enviado para {}: envio de e-mail desativado.", to);
            return;
        }

        if (to == null || to.isBlank()) {
            log.info("E-mail nao enviado: destinatario vazio.");
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("E-mail nao enviado para {}: JavaMailSender nao configurado.", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (from != null && !from.isBlank()) {
                message.setFrom(from);
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception exception) {
            log.warn("Nao foi possivel enviar e-mail para {}.", to, exception);
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
