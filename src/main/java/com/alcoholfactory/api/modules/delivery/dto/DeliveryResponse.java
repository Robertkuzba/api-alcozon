package com.alcoholfactory.api.modules.delivery.dto;

import com.alcoholfactory.api.common.domain.DeliveryStatus;
import com.alcoholfactory.api.modules.order.dto.OrderDeliveryDetailsResponse;

import java.time.Instant;

public record DeliveryResponse(
        Long id,
        Long orderId,
        String orderNumber,
        String clientOrderNumber,
        Long courierId,
        String courierEmail,
        DeliveryStatus status,
        String addressSnapshot,
        OrderDeliveryDetailsResponse deliveryDetails,
        String customerEmail,
        Instant startedAt,
        Instant deliveredAt
) {}
