package com.alcoholfactory.api.modules.security.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.config.AppSecurityProperties;
import com.alcoholfactory.api.modules.security.dto.AppCheckRequest;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class AppSecurityService {

  private static final String DEFAULT_PACKAGE_NAME = "com.alkozon.app";
  private static final int DEFAULT_MIN_VERSION_CODE = 1;

  /** Domyślny debug SHA (Flutter/Android dev). */
  private static final String DEFAULT_DEBUG_SHA256 =
      "e1b17830399a952b8ff905023d5dc98f0a202cbb18941beb06000717341ac7f6";

  private final String expectedPackageName;
  private final int minVersionCode;
  private final Set<String> allowedSha256;

  public AppSecurityService(AppSecurityProperties appSecurityProperties) {
    AppSecurityProperties.Android android =
        appSecurityProperties != null ? appSecurityProperties.android() : null;
    this.expectedPackageName = resolvePackageName(android);
    this.minVersionCode = resolveMinVersionCode(android);
    this.allowedSha256 = resolveAllowedShas(android);
    log.info(
        "App-check config: package={}, minVersionCode={}, allowedShaCount={}",
        expectedPackageName,
        minVersionCode,
        allowedSha256.size());
  }

  public void verify(AppCheckRequest request) {
    if (!isAllowed(request)) {
      log.warn(
          "App-check rejected: platform={}, package={}, versionCode={}, shaPrefix={}",
          request.platform(),
          request.packageName(),
          request.versionCode(),
          shaPrefix(request.signingCertSha256()));
      throw new BusinessException(HttpStatus.FORBIDDEN, "App not allowed");
    }
  }

  boolean isAllowed(AppCheckRequest request) {
    if (!"android".equalsIgnoreCase(request.platform())) {
      return false;
    }
    if (!Objects.equals(expectedPackageName, request.packageName())) {
      return false;
    }
    Integer versionCode = request.versionCode();
    if (versionCode == null || versionCode < minVersionCode) {
      return false;
    }
    String sha = normalizeSha256(request.signingCertSha256());
    return allowedSha256.contains(sha);
  }

  private static String resolvePackageName(AppSecurityProperties.Android android) {
    if (android != null && StringUtils.hasText(android.packageName())) {
      return android.packageName().trim();
    }
    return DEFAULT_PACKAGE_NAME;
  }

  private static int resolveMinVersionCode(AppSecurityProperties.Android android) {
    if (android != null && android.minVersionCode() > 0) {
      return android.minVersionCode();
    }
    return DEFAULT_MIN_VERSION_CODE;
  }

  private static Set<String> resolveAllowedShas(AppSecurityProperties.Android android) {
    List<String> raw = android != null ? android.allowedSigningCertSha256() : null;
    Set<String> configured =
        raw == null
            ? Set.of()
            : raw.stream()
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
