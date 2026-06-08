package com.alcoholfactory.api.notification;

import com.alcoholfactory.api.common.domain.OrderStatus;

/** Payload STOMP / danych FCM dla klientów realtime (Web, mobilka, desktop). */
public record OrderRealtimeEvent(
    OrderRealtimeEventType type,
    long orderId,
    String clientOrderNumber,
    String status,
    Long deliveryId,
    Long courierUserId) {

  public static OrderRealtimeEvent of(
      OrderRealtimeEventType type, long orderId, String clientOrderNumber, OrderStatus status) {
    return new OrderRealtimeEvent(type, orderId, clientOrderNumber, status.name(), null, null);
  }

  public OrderRealtimeEvent withDelivery(long deliveryId) {
    return new OrderRealtimeEvent(
        type, orderId, clientOrderNumber, status, deliveryId, courierUserId);
  }

  public OrderRealtimeEvent withCourier(long courierUserId) {
    return new OrderRealtimeEvent(
        type, orderId, clientOrderNumber, status, deliveryId, courierUserId);
  }
}
