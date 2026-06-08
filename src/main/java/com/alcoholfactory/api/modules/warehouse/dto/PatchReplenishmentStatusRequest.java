package com.alcoholfactory.api.modules.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PatchReplenishmentStatusRequest(
    @NotBlank
        @Pattern(regexp = "RECEIVED|COMPLETED", message = "status must be RECEIVED or COMPLETED")
        String status) {}
