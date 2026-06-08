package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.validation.ValidationPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank
        @Pattern(
            regexp = ValidationPatterns.CLIENT_ORDER_NUMBER,
            message = "Invalid client order number")
        @Size(max = 50)
        String clientOrderNumber,
    @NotEmpty @Valid List<OrderLineRequest> items,
    @NotNull @Valid DeliveryDetailsRequest delivery) {}
