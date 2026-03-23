package com.alcoholfactory.api.modules.admin.dto;

import com.alcoholfactory.api.common.domain.UserRole;
import jakarta.validation.constraints.NotNull;

public record PatchUserRequest(
        @NotNull UserRole role,
        @NotNull Boolean active,
        @NotNull Boolean courier
) {}
