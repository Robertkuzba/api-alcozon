package com.alcoholfactory.api.modules.auth.service;

import com.alcoholfactory.api.common.domain.UserRole;
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

    public void sendNewPassword(String toEmail, String plainPassword, UserRole role) {
        boolean manager = role == UserRole.MANAGER;
        String subject = manager
                ? "Alkozon — nowe haslo do konta managera"
                : "Alkozon — nowe haslo do konta pracownika";
        String loginHint = manager
                ? "Zaloguj sie w aplikacji Desktop (staff/login) i przy pierwszym logowaniu potwierdz urzadzenie kodem 2FA z e-maila."
                : "Zaloguj sie w aplikacji mobilnej (staff/login) i przy pierwszym logowaniu potwierdz urzadzenie kodem 2FA z e-maila.";
        String body = """
                Otrzymujesz nowe haslo do aplikacji Alkozon (%s):

                %s

                %s
                Jesli nie prosiles o reset — skontaktuj sie z administratorem.
                """.formatted(
                manager ? "konto managera" : "konto pracownika",
                plainPassword,
                loginHint);

        if (mailProperties.logOnly()) {
            log.info(
                    "Staff password reset mail (log-only) role={} to={} password={}",
                    role,
                    toEmail,
                    plainPassword);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.from());
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
            log.info("Staff password reset email sent role={} to={}", role, toEmail);
        } catch (Exception ex) {
            log.error("Staff password reset email failed role={} to={}. Cause: {}", role, toEmail, ex.getMessage());
            log.warn(
                    "Staff password reset mail (fallback log-only) role={} to={} password={}",
                    role,
                    toEmail,
                    plainPassword);
        }
    }
}
