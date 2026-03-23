package com.alcoholfactory.api.modules.delivery.dto;

import com.alcoholfactory.api.common.domain.DeliveryStatus;

import java.time.Instant;

public record DeliveryResponse(
        Long id,
        Long orderId,
        Long courierId,
        String courierEmail,
        DeliveryStatus status,
        String addressSnapshot,
        String customerEmail,
        Instant startedAt,
        Instant deliveredAt
) {}
