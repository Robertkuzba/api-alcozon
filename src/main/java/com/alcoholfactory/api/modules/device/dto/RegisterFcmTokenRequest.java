package com.alcoholfactory.api.modules.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterFcmTokenRequest(
    @NotBlank @Size(max = 4096) String token, @NotBlank @Size(max = 32) String platform) {}
