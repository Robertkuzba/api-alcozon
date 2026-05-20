package com.alcoholfactory.api.modules.security.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.config.AppSecurityProperties;
import com.alcoholfactory.api.modules.security.dto.AppCheckRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppSecurityService {

    private final AppSecurityProperties appSecurityProperties;

    public void verify(AppCheckRequest request) {
        if (!isAllowed(request)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "App not allowed");
        }
    }

    boolean isAllowed(AppCheckRequest request) {
        AppSecurityProperties.Android android = appSecurityProperties.android();
        if (android == null) {
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

    private static Set<String> allowedShaSet(AppSecurityProperties.Android android) {
        return android.allowedSigningCertSha256().stream()
                .map(AppSecurityService::normalizeSha256)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private static String normalizeSha256(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace(":", "").toLowerCase(Locale.ROOT).trim();
    }
}
