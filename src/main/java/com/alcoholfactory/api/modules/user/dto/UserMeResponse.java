package com.alcoholfactory.api.modules.user.dto;

import com.alcoholfactory.api.common.domain.UserRole;
import java.time.Instant;

public record UserMeResponse(
    Long id,
    String email,
    UserRole role,
    String firstName,
    String lastName,
    String phone,
    boolean courier,
    boolean active,
    Instant ageConfirmedAt) {}
