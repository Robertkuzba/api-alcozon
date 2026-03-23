package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty @Valid List<OrderLineRequest> items,
        @NotBlank
        @Pattern(regexp = ValidationPatterns.SAFE_TEXT, message = "Invalid characters in address")
        @Size(max = 2000)
        String deliveryAddress
) {}
