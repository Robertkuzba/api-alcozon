package com.alcoholfactory.api.modules.dev.service;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.delivery.dto.DeliveryResponse;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.delivery.service.DeliveryService;
import com.alcoholfactory.api.modules.dev.dto.NotificationTestHookResponse;
import com.alcoholfactory.api.modules.inventory.domain.ProductStock;
import com.alcoholfactory.api.modules.inventory.repository.ProductStockRepository;
import com.alcoholfactory.api.modules.order.dto.CreateOrderRequest;
import com.alcoholfactory.api.modules.order.dto.DeliveryDetailsRequest;
import com.alcoholfactory.api.modules.order.dto.OrderLineRequest;
import com.alcoholfactory.api.modules.order.dto.OrderResponse;
import com.alcoholfactory.api.modules.order.service.OrderService;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tworzy zamówienie sklepowe i przypisuje dostawę do {@code employee@example.com} (pełna ścieżka
 * notifierów: create → IN_DELIVERY → assign / FCM kuriera).
 */
@Service
@RequiredArgsConstructor
public class NotificationTestHookService {

  private static final String CUSTOMER_EMAIL = "customer@example.com";
  private static final String EMPLOYEE_EMAIL = "employee@example.com";

  private final UserRepository userRepository;
  private final ProductStockRepository productStockRepository;
  private final OrderService orderService;
  private final DeliveryService deliveryService;
  private final DeliveryRepository deliveryRepository;

  @Transactional
  public NotificationTestHookResponse createOrderAndAssignToSeedEmployee() {
    User customer =
        userRepository
            .findByEmail(CUSTOMER_EMAIL)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.PRECONDITION_FAILED,
                        "Brak konta demo " + CUSTOMER_EMAIL + " — uruchom DataInitializer"));
    User employee =
        userRepository
            .findByEmail(EMPLOYEE_EMAIL)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.PRECONDITION_FAILED, "Brak konta demo " + EMPLOYEE_EMAIL));

    long productId =
        productStockRepository.findAll().stream()
            .filter(s -> s.getQuantity() > 0)
            .findFirst()
            .map(ProductStock::getId)
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.PRECONDITION_FAILED, "Brak produktu ze stanem magazynowym"));

    String clientOrderNumber = "T" + System.nanoTime();

    OrderResponse created =
        orderService.create(
            customer.getId(),
            new CreateOrderRequest(
                clientOrderNumber,
                List.of(new OrderLineRequest(productId, 1)),
                new DeliveryDetailsRequest(
                    "Test FCM",
                    "Cesarzowicka 100",
                    "Wrocław",
                    "52-408",
                    "Polska",
                    "Hook test FCM kurier",
                    "Płatność przy odbiorze")));

    OrderResponse inDelivery = step(created.id(), OrderStatus.IN_PRODUCTION);
    inDelivery = step(inDelivery.id(), OrderStatus.IN_PACKING);
    inDelivery = step(inDelivery.id(), OrderStatus.IN_DELIVERY);

    long deliveryId =
        deliveryRepository
            .findByOrderId(inDelivery.id())
            .orElseThrow(
                () ->
                    new BusinessException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Brak Delivery po IN_DELIVERY"))
            .getId();

    DeliveryResponse assigned = deliveryService.assign(deliveryId, employee.getId());

    return new NotificationTestHookResponse(
        assigned.orderId(),
        assigned.id(),
        assigned.clientOrderNumber(),
        employee.getId(),
        employee.getEmail(),
        "FCM kuriera: zaloguj się jako employee@, POST /api/devices/fcm z tokenem, potem wywołaj"
            + " ten hook.");
  }

  private OrderResponse step(long orderId, OrderStatus status) {
    return orderService.updateStatus(orderId, status);
  }
}
