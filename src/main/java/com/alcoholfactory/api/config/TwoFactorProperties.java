package com.alcoholfactory.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.two-factor")
public record TwoFactorProperties(
    boolean enabled, int codeTtlSeconds, int challengeTtlSeconds, String fixedCodeForTests) {
  public TwoFactorProperties {
    if (codeTtlSeconds <= 0) {
      codeTtlSeconds = 600;
    }
    if (challengeTtlSeconds <= 0) {
      challengeTtlSeconds = 900;
    }
  }

  public boolean useFixedCode() {
    return fixedCodeForTests != null && !fixedCodeForTests.isBlank();
  }
}
