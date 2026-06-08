package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.common.domain.OrderStatus;
import java.time.Instant;

/** Publiczne śledzenie zamówienia — bez adresu, pozycji ani danych osobowych poza statusem. */
public record OrderTrackResponse(
    long orderId,
    String clientOrderNumber,
    OrderStatus status,
    Instant createdAt,
    Instant updatedAt) {}
