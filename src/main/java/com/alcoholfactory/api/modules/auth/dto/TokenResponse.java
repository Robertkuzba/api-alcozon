package com.alcoholfactory.api.modules.auth.dto;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.user.domain.User;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        Long userId,
        String email,
        UserRole role,
        String firstName,
        String lastName
) {
    public static TokenResponse of(String access, String refresh, long accessTtl, User user) {
        return new TokenResponse(
                access,
                refresh,
                "Bearer",
                accessTtl,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
