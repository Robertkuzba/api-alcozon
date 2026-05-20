package com.alcoholfactory.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(Android android) {

    public record Android(
            String packageName,
            int minVersionCode,
            List<String> allowedSigningCertSha256
    ) {}
}
