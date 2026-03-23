package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.CustomOrderStatus;

import java.time.Instant;
import java.util.Map;

public record CustomOrderResponse(
        Long id,
        Long customerId,
        String description,
        Map<String, Object> preferences,
        CustomOrderStatus status,
        Long assignedToId,
        Instant createdAt,
        Instant updatedAt
) {}
