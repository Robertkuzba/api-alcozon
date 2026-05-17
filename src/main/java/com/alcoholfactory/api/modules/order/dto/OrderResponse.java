package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String clientOrderNumber,
        Long customerId,
        OrderStatus status,
        String deliveryAddress,
        OrderDeliveryDetailsResponse deliveryDetails,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant deliveredAt,
        List<OrderItemResponse> items
) {}
