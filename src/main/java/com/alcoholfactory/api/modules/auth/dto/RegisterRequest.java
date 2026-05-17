package com.alcoholfactory.api.modules.auth.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank
        @Pattern(regexp = ValidationPatterns.PASSWORD_STRONG, message = "Password must be 8+ chars with upper, lower, digit and special")
        String password,
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT, message = "Invalid characters in first name")
        @Size(max = 100) String firstName,
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT, message = "Invalid characters in last name")
        @Size(max = 100) String lastName,
        @NotNull(message = "ageConfirmed is required")
        Boolean ageConfirmed
) {
    @AssertTrue(message = "Musisz potwierdzić pełnoletność (18+)")
    public boolean isAdultConfirmed() {
        return Boolean.TRUE.equals(ageConfirmed);
    }
}
