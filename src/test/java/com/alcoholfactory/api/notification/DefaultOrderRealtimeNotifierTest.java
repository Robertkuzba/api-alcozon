package com.alcoholfactory.api.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alcoholfactory.api.common.domain.DeliveryStatus;
import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import com.alcoholfactory.api.modules.user.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class DefaultOrderRealtimeNotifierTest {

  @Mock SimpMessagingTemplate messagingTemplate;

  @Mock FcmStaffOrderPushService fcmPush;

  @Mock DeliveryRepository deliveryRepository;

  @InjectMocks DefaultOrderRealtimeNotifier notifier;

  @Test
  void onOrderCreated_notifiesCustomerStaffAndFcm() {
    CustomerOrder order = order(7L, "430721", customer("buyer@test.com"));

    notifier.onOrderCreated(order);

    verify(messagingTemplate)
        .convertAndSendToUser(
            eq("buyer@test.com"),
            eq(OrderRealtimeDestinations.CUSTOMER_QUEUE),
            org.mockito.ArgumentMatchers.any(OrderRealtimeEvent.class));
    verify(messagingTemplate)
        .convertAndSend(
            eq(OrderRealtimeDestinations.STAFF_TOPIC),
            org.mockito.ArgumentMatchers.any(OrderRealtimeEvent.class));
    verify(fcmPush)
        .notifyStaffRoles(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(OrderRealtimeEvent.class),
            eq(FcmStaffOrderPushService.STAFF_ROLES));
  }

  @Test
  void onOrderStatusChanged_inDelivery_publishesDispatchTopic() {
    CustomerOrder order = order(3L, "100200", customer("buyer@test.com"));
    when(deliveryRepository.findByOrderId(3L)).thenReturn(Optional.of(delivery(9L, order)));

    notifier.onOrderStatusChanged(order, OrderStatus.IN_DELIVERY);

    var captor = org.mockito.ArgumentCaptor.forClass(OrderRealtimeEvent.class);
    verify(messagingTemplate)
        .convertAndSend(eq(OrderRealtimeDestinations.DISPATCH_TOPIC), captor.capture());
    assertThat(captor.getValue().type()).isEqualTo(OrderRealtimeEventType.DISPATCH_PENDING);
    assertThat(captor.getValue().deliveryId()).isEqualTo(9L);
  }

  @Test
  void onDeliveryAssigned_notifiesCourierQueue() {
    CustomerOrder order = order(5L, "555111", customer("buyer@test.com"));
    User courier = User.builder().id(2L).email("courier@test.com").role(UserRole.EMPLOYEE).build();
    Delivery delivery = delivery(11L, order);
    delivery.setCourier(courier);

    notifier.onDeliveryAssigned(delivery);

    verify(messagingTemplate)
        .convertAndSendToUser(
            eq("courier@test.com"),
            eq(OrderRealtimeDestinations.COURIER_QUEUE),
            org.mockito.ArgumentMatchers.any(OrderRealtimeEvent.class));
    verify(fcmPush)
        .notifyUser(
            eq(2L),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(OrderRealtimeEvent.class));
  }

  private static CustomerOrder order(long id, String clientNumber, User customer) {
    return CustomerOrder.builder()
        .id(id)
        .clientOrderNumber(clientNumber)
        .customer(customer)
        .status(OrderStatus.SUBMITTED)
        .build();
  }

  private static User customer(String email) {
    return User.builder().id(1L).email(email).role(UserRole.CUSTOMER).build();
  }

  private static Delivery delivery(long id, CustomerOrder order) {
    return Delivery.builder().id(id).order(order).status(DeliveryStatus.PENDING).build();
  }
}
