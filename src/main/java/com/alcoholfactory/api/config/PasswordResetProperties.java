package com.alcoholfactory.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.password-reset")
public record PasswordResetProperties(
    /**
     * Tylko testy (profil test / DynamicPropertySource) — deterministyczne hasło zamiast losowego.
     */
    String fixedPasswordForTests) {
  public boolean useFixedPassword() {
    return fixedPasswordForTests != null && !fixedPasswordForTests.isBlank();
  }
}
