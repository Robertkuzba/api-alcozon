package com.alcoholfactory.api.modules.admin.dto;

import com.alcoholfactory.api.common.domain.UserRole;
import java.time.Instant;

public record UserAdminResponse(
    Long id,
    String email,
    UserRole role,
    boolean active,
    boolean courier,
    Instant ageConfirmedAt) {}
