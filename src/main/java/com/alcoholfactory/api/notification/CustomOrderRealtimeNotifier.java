package com.alcoholfactory.api.notification;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CustomOrderRealtimeNotifier {

  private final SimpMessagingTemplate messagingTemplate;
  private final FcmStaffOrderPushService fcmPush;
  private final DeliveryRepository deliveryRepository;

  public void onCustomOrderCreated(CustomOrder order) {
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
            OrderRealtimeEventType.ORDER_SUBMITTED,
            order.getId(),
            clientOrderNumber(order),
            OrderStatus.SUBMITTED);
    toCustomer(order, event);
    toStaff(event);
    fcmPush.notifyStaffRoles(
        "Nowe zamówienie własne",
        label(order) + " — do realizacji",
        event,
        FcmStaffOrderPushService.STAFF_ROLES);
  }

  public void onCustomOrderStatusChanged(CustomOrder order, OrderStatus newStatus) {
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
            OrderRealtimeEventType.ORDER_STATUS_CHANGED,
            order.getId(),
            clientOrderNumber(order),
            newStatus);
    toCustomer(order, event);
    toStaff(event);

    if (newStatus == OrderStatus.IN_DELIVERY) {
      Long deliveryId =
          deliveryRepository.findByCustomOrderId(order.getId()).map(Delivery::getId).orElse(null);
      OrderRealtimeEvent dispatch =
          new OrderRealtimeEvent(
              OrderRealtimeEventType.DISPATCH_PENDING,
              order.getId(),
              clientOrderNumber(order),
              newStatus.name(),
              deliveryId,
              null);
      messagingTemplate.convertAndSend(OrderRealtimeDestinations.DISPATCH_TOPIC, dispatch);
      fcmPush.notifyStaffRoles(
          "Gotowe do wysyłki (własne)",
          label(order) + " — przypisz kuriera (desktop)",
          dispatch,
          FcmStaffOrderPushService.MANAGER_ROLES);
    }
  }

  public void onCustomDeliveryAssigned(Delivery delivery) {
    CustomOrder order = delivery.getCustomOrder();
    if (order == null) {
      return;
    }
    var courier = delivery.getCourier();
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
                OrderRealtimeEventType.DELIVERY_ASSIGNED,
                order.getId(),
                clientOrderNumber(order),
                order.getStatus())
            .withDelivery(delivery.getId());
    if (courier != null) {
      event = event.withCourier(courier.getId());
    }
    toStaff(event);
    if (courier != null && StringUtils.hasText(courier.getEmail())) {
      messagingTemplate.convertAndSendToUser(
          courier.getEmail(), OrderRealtimeDestinations.COURIER_QUEUE, event);
      fcmPush.notifyUser(
          courier.getId(), "Nowa dostawa (własne)", label(order) + " — adres w aplikacji", event);
    }
  }

  public void onCustomOrderDelivered(CustomOrder order, Delivery delivery) {
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
            OrderRealtimeEventType.ORDER_DELIVERED,
            order.getId(),
            clientOrderNumber(order),
            OrderStatus.DELIVERED);
    if (delivery != null) {
      event = event.withDelivery(delivery.getId());
      if (delivery.getCourier() != null) {
        event = event.withCourier(delivery.getCourier().getId());
      }
    }
    toCustomer(order, event);
    toStaff(event);
  }

  private void toCustomer(CustomOrder order, OrderRealtimeEvent event) {
    String email = order.getCustomer().getEmail();
    if (!StringUtils.hasText(email)) {
      return;
    }
    messagingTemplate.convertAndSendToUser(email, OrderRealtimeDestinations.CUSTOMER_QUEUE, event);
  }

  private void toStaff(OrderRealtimeEvent event) {
    messagingTemplate.convertAndSend(OrderRealtimeDestinations.STAFF_TOPIC, event);
  }

  private static String label(CustomOrder order) {
    return "Zamówienie własne " + clientOrderNumber(order);
  }

  private static String clientOrderNumber(CustomOrder order) {
    if (StringUtils.hasText(order.getClientOrderNumber())) {
      return order.getClientOrderNumber();
    }
    return "CUSTOM-" + order.getId();
  }
}
