package com.alcoholfactory.api.modules.dev.dto;

public record NotificationTestHookResponse(
        long orderId,
        long deliveryId,
        String clientOrderNumber,
        long courierUserId,
        String courierEmail,
        String hint
) {}
