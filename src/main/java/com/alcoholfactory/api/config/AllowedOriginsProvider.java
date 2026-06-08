package com.alcoholfactory.api.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Łączy originy z {@code application.yml} z listą z zmiennej {@code APP_CORS_ALLOWED_ORIGINS}
 * (wartości rozdzielone przecinkami, np. na Renderze).
 */
@Component
public class AllowedOriginsProvider {

  private final CorsProperties corsProperties;

  @Value("${APP_CORS_ALLOWED_ORIGINS:}")
  private String additionalOriginsEnv;

  public AllowedOriginsProvider(CorsProperties corsProperties) {
    this.corsProperties = corsProperties;
  }

  public List<String> mergedAllowedOrigins() {
    List<String> list = new ArrayList<>(corsProperties.allowedOrigins());
    if (StringUtils.hasText(additionalOriginsEnv)) {
      for (String part : additionalOriginsEnv.split(",")) {
        String trimmed = part.trim();
        if (!trimmed.isEmpty() && !list.contains(trimmed)) {
          list.add(trimmed);
        }
      }
    }
    return List.copyOf(list);
  }
}
