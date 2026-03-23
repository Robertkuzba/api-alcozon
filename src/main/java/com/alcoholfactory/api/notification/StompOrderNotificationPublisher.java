package com.alcoholfactory.api.notification;

import com.alcoholfactory.api.common.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompOrderNotificationPublisher implements OrderNotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishOrderStatusChange(String customerEmail, Long orderId, OrderStatus status) {
        messagingTemplate.convertAndSendToUser(
                customerEmail,
                "/queue/order-updates",
                Map.of(
                        "orderId", orderId,
                        "status", status.name()
                )
        );
    }
}
