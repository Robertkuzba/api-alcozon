package com.alcoholfactory.api.modules.auth.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VerifyDeviceRequest(
        @NotNull UUID challengeId,
        @NotBlank
        @Size(min = 8, max = 128)
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT, message = "Invalid device id")
        String deviceId,
        @NotBlank
        @Pattern(regexp = "^\\d{4}$", message = "Code must be 4 digits")
        String code
) {}
