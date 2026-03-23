package com.alcoholfactory.api.modules.admin.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AnnouncementRequest(
        @NotBlank @Pattern(regexp = ValidationPatterns.SAFE_TEXT) @Size(max = 255) String title,
        @NotBlank @Pattern(regexp = ValidationPatterns.SAFE_TEXT) @Size(max = 8000) String content
) {}
