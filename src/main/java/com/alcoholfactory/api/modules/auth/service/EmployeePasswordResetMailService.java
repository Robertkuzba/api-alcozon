package com.alcoholfactory.api.modules.auth.service;

import com.alcoholfactory.api.config.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeePasswordResetMailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public void sendNewPassword(String toEmail, String plainPassword) {
        String subject = "Alkozon — nowe hasło do konta pracownika";
        String body = """
                Otrzymujesz nowe hasło do aplikacji Alkozon (konto pracownika):

                %s

                Zaloguj się i zmień hasło, jeśli Twoja aplikacja na to pozwala.
                Jeśli nie prosiłeś o reset — skontaktuj się z administratorem.
                """.formatted(plainPassword);

        if (mailProperties.logOnly()) {
            log.info("Employee password reset mail (log-only) to={} password={}", toEmail, plainPassword);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.from());
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
            log.info("Employee password reset email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Employee password reset email failed to={}. Cause: {}", toEmail, ex.getMessage());
            log.warn("Employee password reset mail (fallback log-only) to={} password={}", toEmail, plainPassword);
        }
    }
}
