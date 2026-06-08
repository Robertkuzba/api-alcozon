package com.alcoholfactory.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  private final CorsProperties corsProperties;
  private final AllowedOriginsProvider allowedOriginsProvider;

  public CorsConfig(CorsProperties corsProperties, AllowedOriginsProvider allowedOriginsProvider) {
    this.corsProperties = corsProperties;
    this.allowedOriginsProvider = allowedOriginsProvider;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOriginsProvider.mergedAllowedOrigins());
    config.setAllowedMethods(corsProperties.allowedMethods());
    config.setAllowedHeaders(corsProperties.allowedHeaders());
    config.setExposedHeaders(corsProperties.exposedHeaders());
    config.setAllowCredentials(corsProperties.allowCredentials());
    config.setMaxAge(corsProperties.maxAgeSeconds());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
