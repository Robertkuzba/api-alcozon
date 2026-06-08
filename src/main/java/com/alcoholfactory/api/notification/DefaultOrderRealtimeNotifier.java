package com.alcoholfactory.api.notification;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import com.alcoholfactory.api.modules.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class DefaultOrderRealtimeNotifier implements OrderRealtimeNotifier {

  private final SimpMessagingTemplate messagingTemplate;
  private final FcmStaffOrderPushService fcmPush;
  private final DeliveryRepository deliveryRepository;

  @Override
  public void onOrderCreated(CustomerOrder order) {
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
            OrderRealtimeEventType.ORDER_SUBMITTED,
            order.getId(),
            order.getClientOrderNumber(),
            OrderStatus.SUBMITTED);
    toCustomer(order, event);
    toStaff(event);
    fcmPush.notifyStaffRoles(
        "Nowe zamówienie",
        label(order) + " — do realizacji",
        event,
        FcmStaffOrderPushService.STAFF_ROLES);
  }

  @Override
  public void onOrderStatusChanged(CustomerOrder order, OrderStatus newStatus) {
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
            OrderRealtimeEventType.ORDER_STATUS_CHANGED,
            order.getId(),
            order.getClientOrderNumber(),
            newStatus);
    toCustomer(order, event);
    toStaff(event);

    if (newStatus == OrderStatus.IN_DELIVERY) {
      publishDispatchPending(order, newStatus);
    }
  }

  @Override
  public void onOrderCancelled(CustomerOrder order) {
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
            OrderRealtimeEventType.ORDER_CANCELLED,
            order.getId(),
            order.getClientOrderNumber(),
            OrderStatus.CANCELLED);
    toCustomer(order, event);
    toStaff(event);
  }

  @Override
  public void onDeliveryAssigned(Delivery delivery) {
    if (delivery.getOrder() == null) {
      return;
    }
    CustomerOrder order = delivery.getOrder();
    User courier = delivery.getCourier();
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
                OrderRealtimeEventType.DELIVERY_ASSIGNED,
                order.getId(),
                order.getClientOrderNumber(),
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
          courier.getId(), "Nowa dostawa", label(order) + " — adres w aplikacji", event);
    }
  }

  @Override
  public void onOrderDelivered(CustomerOrder order, Delivery delivery) {
    OrderRealtimeEvent event =
        OrderRealtimeEvent.of(
            OrderRealtimeEventType.ORDER_DELIVERED,
            order.getId(),
            order.getClientOrderNumber(),
            OrderStatus.DELIVERED);
    if (delivery != null) {
      event = event.withDelivery(delivery.getId());
      if (delivery.getCourier() != null) {
        event = event.withCourier(delivery.getCourier().getId());
      }
    }
    toCustomer(order, event);
    toStaff(event);
    if (delivery != null && delivery.getCourier() != null) {
      User courier = delivery.getCourier();
      if (StringUtils.hasText(courier.getEmail())) {
        messagingTemplate.convertAndSendToUser(
            courier.getEmail(), OrderRealtimeDestinations.COURIER_QUEUE, event);
      }
    }
  }

  private void publishDispatchPending(CustomerOrder order, OrderStatus status) {
    Long deliveryId =
        deliveryRepository.findByOrderId(order.getId()).map(Delivery::getId).orElse(null);
    OrderRealtimeEvent dispatch =
        new OrderRealtimeEvent(
            OrderRealtimeEventType.DISPATCH_PENDING,
            order.getId(),
            order.getClientOrderNumber(),
            status.name(),
            deliveryId,
            null);
    messagingTemplate.convertAndSend(OrderRealtimeDestinations.DISPATCH_TOPIC, dispatch);
    fcmPush.notifyStaffRoles(
        "Gotowe do wysyłki",
        label(order) + " — przypisz kuriera (desktop)",
        dispatch,
        FcmStaffOrderPushService.MANAGER_ROLES);
  }

  private void toCustomer(CustomerOrder order, OrderRealtimeEvent event) {
    String email = order.getCustomer().getEmail();
    if (!StringUtils.hasText(email)) {
      return;
    }
    messagingTemplate.convertAndSendToUser(email, OrderRealtimeDestinations.CUSTOMER_QUEUE, event);
  }

  private void toStaff(OrderRealtimeEvent event) {
    messagingTemplate.convertAndSend(OrderRealtimeDestinations.STAFF_TOPIC, event);
  }

  private static String label(CustomerOrder order) {
    return "Zamówienie " + order.getClientOrderNumber();
  }
}
