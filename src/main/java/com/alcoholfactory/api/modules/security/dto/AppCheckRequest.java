package com.alcoholfactory.api.modules.security.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppCheckRequest(
    @NotBlank String platform,
    @NotBlank String packageName,
    String versionName,
    @NotNull @Min(1) Integer versionCode,
    @NotBlank String signingCertSha256) {}
