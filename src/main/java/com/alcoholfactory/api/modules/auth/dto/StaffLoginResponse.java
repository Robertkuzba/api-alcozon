package com.alcoholfactory.api.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StaffLoginResponse(
        boolean verificationRequired,
        TokenResponse tokens,
        UUID challengeId,
        Long expiresInSeconds,
        String message
) {
    public static StaffLoginResponse verified(TokenResponse tokens) {
        return new StaffLoginResponse(false, tokens, null, null, null);
    }

    public static StaffLoginResponse pending(UUID challengeId, long expiresInSeconds) {
        return new StaffLoginResponse(
                true,
                null,
                challengeId,
                expiresInSeconds,
                "Verification code sent to your email"
        );
    }
}
