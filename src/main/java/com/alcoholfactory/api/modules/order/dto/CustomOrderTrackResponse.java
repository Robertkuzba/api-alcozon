package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.OrderStatus;

import java.time.Instant;

public record CustomOrderTrackResponse(
        long customOrderId,
        String clientOrderNumber,
        OrderStatus status,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
