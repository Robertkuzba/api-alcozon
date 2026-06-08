package com.alcoholfactory.api.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(Android android) {

  public record Android(
      String packageName, int minVersionCode, List<String> allowedSigningCertSha256) {}
}
