package com.alcoholfactory.api.modules.order.service;

import com.alcoholfactory.api.common.domain.DeliveryStatus;
import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.inventory.domain.ProductStock;
import com.alcoholfactory.api.modules.inventory.repository.ProductStockRepository;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import com.alcoholfactory.api.modules.order.domain.OrderDeliveryDetails;
import com.alcoholfactory.api.modules.order.domain.OrderItem;
import com.alcoholfactory.api.modules.order.dto.CreateOrderRequest;
import com.alcoholfactory.api.modules.order.dto.DeliveryDetailsRequest;
import com.alcoholfactory.api.modules.order.dto.OrderDeliveryDetailsResponse;
import com.alcoholfactory.api.modules.order.dto.OrderItemResponse;
import com.alcoholfactory.api.modules.order.dto.OrderLineRequest;
import com.alcoholfactory.api.modules.order.dto.OrderResponse;
import com.alcoholfactory.api.modules.order.dto.OrderTrackResponse;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import com.alcoholfactory.api.modules.order.util.OrderNumbers;
import com.alcoholfactory.api.modules.order.util.OrderStatusTransitions;
import com.alcoholfactory.api.modules.product.domain.Product;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.notification.OrderRealtimeNotifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final CustomerOrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ProductStockRepository productStockRepository;
  private final UserRepository userRepository;
  private final DeliveryRepository deliveryRepository;
  private final OrderRealtimeNotifier orderRealtimeNotifier;

  @Transactional
  public OrderResponse create(Long userId, CreateOrderRequest req) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
    if (user.getRole() != UserRole.CUSTOMER || user.getAgeConfirmedAt() == null) {
      throw new BusinessException(
          HttpStatus.FORBIDDEN,
          "Zamówienie wymaga konta CUSTOMER z potwierdzoną pełnoletnością (18+)");
    }

    String clientOrderNumber = req.clientOrderNumber().trim();
    if (orderRepository.existsByClientOrderNumber(clientOrderNumber)) {
      throw new BusinessException(HttpStatus.CONFLICT, "Numer zamówienia jest już użyty");
    }

    CustomerOrder order =
        CustomerOrder.builder()
            .customer(user)
            .status(OrderStatus.SUBMITTED)
            .clientOrderNumber(clientOrderNumber)
            .deliveryDetails(toEmbeddable(req.delivery()))
            .totalAmount(BigDecimal.ZERO)
            .build();

    BigDecimal total = BigDecimal.ZERO;
    for (OrderLineRequest line : req.items()) {
      Product product =
          productRepository
              .findById(line.productId())
              .filter(Product::isActive)
              .orElseThrow(
                  () ->
                      new BusinessException(
                          HttpStatus.BAD_REQUEST, "Invalid product: " + line.productId()));
      ProductStock stock =
          productStockRepository
              .findById(product.getId())
              .orElseThrow(
                  () ->
                      new BusinessException(
                          HttpStatus.BAD_REQUEST, "Brak wpisu magazynowego dla produktu"));
      if (stock.getQuantity() < line.quantity()) {
        throw new BusinessException(
            HttpStatus.BAD_REQUEST, "Niewystarczający stan magazynowy: " + product.getName());
      }
      stock.setQuantity(stock.getQuantity() - line.quantity());

      OrderItem item =
          OrderItem.builder()
              .order(order)
              .product(product)
              .quantity(line.quantity())
              .unitPrice(product.getPrice())
              .build();
      order.getItems().add(item);
      total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
    }
    order.setTotalAmount(total);
    orderRepository.save(order);
    orderRealtimeNotifier.onOrderCreated(order);
    return toResponse(orderRepository.findDetailById(order.getId()).orElse(order));
  }

  /**
   * Publiczne śledzenie: ten sam komunikat błędu przy złym ID lub złym e-mailu (ograniczenie
   * enumeracji). {@code orderRef}: id techniczne, {@code ORD-{id}} (kompatybilność) lub {@code
   * clientOrderNumber}.
   */
  @Transactional(readOnly = true)
  public OrderTrackResponse trackPublic(String orderRef, String email) {
    String normalized = email == null ? "" : email.trim().toLowerCase();
    if (normalized.isEmpty()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid email");
    }
    CustomerOrder order = resolveOrder(orderRef);
    if (!order.getCustomer().getEmail().equalsIgnoreCase(normalized)) {
      throw new BusinessException(HttpStatus.NOT_FOUND, "Order not found");
    }
    return new OrderTrackResponse(
        order.getId(),
        order.getClientOrderNumber(),
        order.getStatus(),
        order.getCreatedAt(),
        order.getUpdatedAt());
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> myOrders(Long userId) {
    return orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId).stream()
        .map(o -> orderRepository.findDetailById(o.getId()).orElse(o))
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public OrderResponse getByRef(String orderRef, Long currentUserId, boolean managerOrEmployee) {
    CustomerOrder order =
        orderRepository
            .findDetailById(resolveOrder(orderRef).getId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
    if (!managerOrEmployee && !order.getCustomer().getId().equals(currentUserId)) {
      throw new BusinessException(HttpStatus.FORBIDDEN, "Brak dostępu do zamówienia");
    }
    return toResponse(order);
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> listAll(Pageable pageable) {
    return orderRepository
        .findAll(pageable)
        .map(o -> orderRepository.findDetailById(o.getId()).orElse(o))
        .map(this::toResponse);
  }

  /**
   * Zamówienia {@link OrderStatus#IN_DELIVERY} z dostawą przypisaną do podanego kuriera. MANAGER:
   * dowolny {@code courierUserId}; EMPLOYEE: tylko własne id.
   */
  @Transactional(readOnly = true)
  public List<OrderResponse> forCourier(Long courierUserId, Long actorUserId, boolean manager) {
    if (!manager && !courierUserId.equals(actorUserId)) {
      throw new BusinessException(HttpStatus.FORBIDDEN, "Brak dostępu");
    }
    User courier =
        userRepository
            .findById(courierUserId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
    if (!courier.isActive()) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "User inactive");
    }
    if (!courier.isCourier()
        && courier.getRole() != UserRole.EMPLOYEE
        && courier.getRole() != UserRole.MANAGER) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "User is not a courier");
    }
    return orderRepository
        .findInDeliveryAssignedToCourier(courierUserId, OrderStatus.IN_DELIVERY)
        .stream()
        .map(o -> orderRepository.findDetailById(o.getId()).orElse(o))
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
    CustomerOrder order =
        orderRepository
            .findDetailById(orderId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
    OrderStatus old = order.getStatus();
    if (!isAllowedTransition(old, newStatus)) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST, "Niedozwolona zmiana statusu: " + old + " -> " + newStatus);
    }
    order.setStatus(newStatus);
    if (newStatus == OrderStatus.DELIVERED) {
      order.setDeliveredAt(Instant.now());
    }
    if (newStatus == OrderStatus.IN_DELIVERY) {
      ensureDelivery(order);
    }
    if (newStatus == OrderStatus.DELIVERED) {
      deliveryRepository
          .findByOrderId(orderId)
          .ifPresent(
              d -> {
                d.setStatus(DeliveryStatus.DELIVERED);
                d.setDeliveredAt(Instant.now());
              });
    }
    orderRepository.save(order);
    if (newStatus == OrderStatus.DELIVERED) {
      deliveryRepository
          .findByOrderId(orderId)
          .ifPresentOrElse(
              d -> orderRealtimeNotifier.onOrderDelivered(order, d),
              () -> orderRealtimeNotifier.onOrderStatusChanged(order, newStatus));
    } else {
      orderRealtimeNotifier.onOrderStatusChanged(order, newStatus);
    }
    return toResponse(orderRepository.findDetailById(orderId).orElseThrow());
  }

  @Transactional
  public OrderResponse cancel(String orderRef, Long customerId) {
    CustomerOrder order =
        orderRepository
            .findDetailById(resolveOrder(orderRef).getId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
    if (!order.getCustomer().getId().equals(customerId)) {
      throw new BusinessException(HttpStatus.FORBIDDEN, "Brak dostępu");
    }
    if (order.getStatus() != OrderStatus.SUBMITTED) {
      throw new BusinessException(
          HttpStatus.BAD_REQUEST, "Można anulować tylko zamówienie w statusie SUBMITTED");
    }
    for (OrderItem item : order.getItems()) {
      ProductStock stock = productStockRepository.findById(item.getProduct().getId()).orElseThrow();
      stock.setQuantity(stock.getQuantity() + item.getQuantity());
    }
    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);
    orderRealtimeNotifier.onOrderCancelled(order);
    return toResponse(orderRepository.findDetailById(order.getId()).orElseThrow());
  }

  private void ensureDelivery(CustomerOrder order) {
    if (deliveryRepository.findByOrderId(order.getId()).isPresent()) {
      return;
    }
    Delivery d =
        Delivery.builder()
            .order(order)
            .clientOrderNumber(order.getClientOrderNumber())
            .status(DeliveryStatus.PENDING)
            .deliveryDetails(OrderDeliveryDetails.copyOf(order.getDeliveryDetails()))
            .build();
    deliveryRepository.save(d);
  }

  private CustomerOrder resolveOrder(String orderRef) {
    String raw = orderRef.trim();
    Long id = OrderNumbers.parseId(raw);
    if (id != null) {
      var byId = orderRepository.findByIdWithCustomer(id);
      if (byId.isPresent()) {
        return byId.get();
      }
    }
    return orderRepository
        .findByClientOrderNumberWithCustomer(raw)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
  }

  private static OrderDeliveryDetails toEmbeddable(DeliveryDetailsRequest req) {
    String country = req.country();
    if (country == null || country.isBlank()) {
      country = "Polska";
    }
    return OrderDeliveryDetails.builder()
        .recipientName(req.recipientName().trim())
        .streetAddress(req.streetAddress().trim())
        .city(req.city().trim())
        .postalCode(normalizePostalCode(req.postalCode()))
        .country(country.trim())
        .deliveryNotes(trimToNull(req.deliveryNotes()))
        .paymentMethod(trimToNull(req.paymentMethod()))
        .build();
  }

  private static String normalizePostalCode(String postalCode) {
    String raw = postalCode.trim().replace(" ", "");
    if (raw.matches("^\\d{5}$")) {
      return raw.substring(0, 2) + "-" + raw.substring(2);
    }
    return raw;
  }

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
    return OrderStatusTransitions.isAllowed(from, to);
  }

  public OrderResponse toResponse(CustomerOrder o) {
    List<OrderItemResponse> items =
        o.getItems() == null
            ? List.of()
            : o.getItems().stream()
                .map(
                    i ->
                        new OrderItemResponse(
                            i.getProduct().getId(),
                            i.getProduct().getName(),
                            i.getQuantity(),
                            i.getUnitPrice()))
                .toList();
    return new OrderResponse(
        o.getId(),
        o.getClientOrderNumber(),
        o.getCustomer().getId(),
        o.getStatus(),
        OrderDeliveryDetailsResponse.from(o.getDeliveryDetails()),
        o.getTotalAmount(),
        o.getCreatedAt(),
        o.getDeliveredAt(),
        items);
  }
}
