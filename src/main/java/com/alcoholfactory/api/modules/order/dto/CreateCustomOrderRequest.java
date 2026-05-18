package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateCustomOrderRequest(
        @NotBlank
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT)
        @Size(max = 5000)
        String description,
        @Pattern(regexp = ValidationPatterns.CLIENT_ORDER_NUMBER, message = "Invalid client order number")
        @Size(max = 50)
        String clientOrderNumber,
        Map<String, Object> preferences
) {}
