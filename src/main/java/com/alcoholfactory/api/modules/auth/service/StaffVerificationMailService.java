package com.alcoholfactory.api.modules.auth.service;

import com.alcoholfactory.api.config.MailProperties;
import com.alcoholfactory.api.config.TwoFactorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffVerificationMailService {

  private final JavaMailSender mailSender;
  private final MailProperties mailProperties;
  private final TwoFactorProperties twoFactorProperties;

  public void sendVerificationCode(String toEmail, String plainCode) {
    String subject = "Alkozon — kod weryfikacji urządzenia";
    String body =
        """
        Twój kod weryfikacji: %s

        Kod jest ważny przez %d minut.
        Jeśli to nie Ty — zignoruj tę wiadomość.
        """
            .formatted(plainCode, twoFactorProperties.codeTtlSeconds() / 60);

    if (mailProperties.logOnly()) {
      log.info("2FA mail (log-only) to={} code={}", toEmail, plainCode);
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(mailProperties.from());
    message.setTo(toEmail);
    message.setSubject(subject);
    message.setText(body);
    try {
      mailSender.send(message);
      log.info("2FA verification email sent to {}", toEmail);
    } catch (Exception ex) {
      // SMTP misconfigured (Mailjet sender, credentials) must not break staff/login with 500
      log.error("2FA email failed to={} — use code from log. Cause: {}", toEmail, ex.getMessage());
      log.warn("2FA mail (fallback log-only) to={} code={}", toEmail, plainCode);
    }
  }
}
