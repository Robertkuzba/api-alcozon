package com.alcoholfactory.api.notification;

import com.alcoholfactory.api.common.domain.OrderStatus;

public interface OrderNotificationPublisher {

    void publishOrderStatusChange(String customerEmail, Long orderId, OrderStatus status);
}
