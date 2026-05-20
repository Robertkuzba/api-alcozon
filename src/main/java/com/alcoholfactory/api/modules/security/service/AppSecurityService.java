package com.alcoholfactory.api.modules.security.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.config.AppSecurityProperties;
import com.alcoholfactory.api.modules.security.dto.AppCheckRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppSecurityService {

    /** Domyślny debug SHA (Flutter/Android dev) — fallback gdy allowlista pusta po złym env na Renderze. */
    private static final String DEFAULT_DEBUG_SHA256 =
            "e1b17830399a952b8ff905023d5dc98f0a202cbb18941beb06000717341ac7f6";

    private final AppSecurityProperties appSecurityProperties;

    public void verify(AppCheckRequest request) {
        if (!isAllowed(request)) {
            log.warn(
                    "App-check rejected: platform={}, package={}, versionCode={}, shaPrefix={}",
                    request.platform(),
                    request.packageName(),
                    request.versionCode(),
                    shaPrefix(request.signingCertSha256())
            );
            throw new BusinessException(HttpStatus.FORBIDDEN, "App not allowed");
        }
    }

    boolean isAllowed(AppCheckRequest request) {
        AppSecurityProperties.Android android = appSecurityProperties.android();
        if (android == null) {
            log.warn("App-check: app.security.android not configured");
            return false;
        }
        if (!"android".equalsIgnoreCase(request.platform())) {
            return false;
        }
        if (!android.packageName().equals(request.packageName())) {
            return false;
        }
        String sha = normalizeSha256(request.signingCertSha256());
        if (!allowedShaSet(android).contains(sha)) {
            return false;
        }
        return request.versionCode() >= android.minVersionCode();
    }

    private Set<String> allowedShaSet(AppSecurityProperties.Android android) {
        Set<String> configured = android.allowedSigningCertSha256() == null
                ? Set.of()
                : android.allowedSigningCertSha256().stream()
                        .map(AppSecurityService::normalizeSha256)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());
        if (!configured.isEmpty()) {
            return configured;
        }
        log.warn("App-check: allowed-signing-cert-sha256 empty — using built-in debug SHA fallback");
        return Set.of(DEFAULT_DEBUG_SHA256);
    }

    private static String shaPrefix(String sha) {
        if (sha == null || sha.length() < 8) {
            return sha;
        }
        return sha.substring(0, 8) + "…";
    }

    private static String normalizeSha256(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace(":", "").toLowerCase(Locale.ROOT).trim();
    }
}
