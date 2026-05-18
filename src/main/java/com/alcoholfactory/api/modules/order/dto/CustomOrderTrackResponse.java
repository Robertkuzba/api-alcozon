package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.CustomOrderStatus;

import java.time.Instant;

public record CustomOrderTrackResponse(
        long customOrderId,
        String clientOrderNumber,
        CustomOrderStatus status,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
