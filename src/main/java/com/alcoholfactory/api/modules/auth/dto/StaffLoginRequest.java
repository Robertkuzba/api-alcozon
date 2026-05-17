package com.alcoholfactory.api.modules.auth.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StaffLoginRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotBlank
        @Size(min = 8, max = 128)
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT, message = "Invalid device id")
        String deviceId
) {}
