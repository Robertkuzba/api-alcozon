package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DeliveryDetailsRequest(
    @NotBlank
        @Pattern(
            regexp = ValidationPatterns.SAFE_TEXT,
            message = "Invalid characters in recipient name")
        @Size(max = 200)
        String recipientName,
    @NotBlank
        @Pattern(
            regexp = ValidationPatterns.SAFE_TEXT,
            message = "Invalid characters in street address")
        @Size(max = 500)
        String streetAddress,
    @NotBlank
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT, message = "Invalid characters in city")
        @Size(max = 100)
        String city,
    @NotBlank
        @Pattern(regexp = ValidationPatterns.POSTAL_CODE_PL, message = "Invalid postal code")
        @Size(max = 20)
        String postalCode,
    @Pattern(regexp = ValidationPatterns.SAFE_TEXT, message = "Invalid characters in country")
        @Size(max = 100)
        String country,
    @Pattern(
            regexp = ValidationPatterns.SAFE_TEXT,
            message = "Invalid characters in delivery notes")
        @Size(max = 2000)
        String deliveryNotes,
    @Pattern(
            regexp = ValidationPatterns.SAFE_TEXT,
            message = "Invalid characters in payment method")
        @Size(max = 100)
        String paymentMethod) {}
