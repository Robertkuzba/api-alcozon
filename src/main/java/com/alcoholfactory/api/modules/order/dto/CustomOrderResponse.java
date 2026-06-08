package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.OrderStatus;
import java.time.Instant;
import java.util.Map;

public record CustomOrderResponse(
    Long id,
    String clientOrderNumber,
    Long customerId,
    String description,
    Map<String, Object> preferences,
    OrderStatus status,
    java.time.Instant deliveredAt,
    Long assignedToId,
    Instant createdAt,
    Instant updatedAt) {}
