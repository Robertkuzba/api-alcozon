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
import com.alcoholfactory.api.modules.order.domain.OrderItem;
import com.alcoholfactory.api.modules.order.dto.CreateOrderRequest;
import com.alcoholfactory.api.modules.order.dto.OrderItemResponse;
import com.alcoholfactory.api.modules.order.dto.OrderResponse;
import com.alcoholfactory.api.modules.order.dto.OrderLineRequest;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import com.alcoholfactory.api.modules.product.domain.Product;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.notification.OrderNotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final UserRepository userRepository;
    private final DeliveryRepository deliveryRepository;
    private final OrderNotificationPublisher notificationPublisher;

    @Transactional
    public OrderResponse create(Long userId, CreateOrderRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != UserRole.CUSTOMER || user.getAgeConfirmedAt() == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Zamówienie wymaga konta CUSTOMER z potwierdzoną pełnoletnością (18+)");
        }

        CustomerOrder order = CustomerOrder.builder()
                .customer(user)
                .status(OrderStatus.SUBMITTED)
                .deliveryAddress(req.deliveryAddress())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineRequest line : req.items()) {
            Product product = productRepository.findById(line.productId())
                    .filter(Product::isActive)
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Invalid product: " + line.productId()));
            ProductStock stock = productStockRepository.findById(product.getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Brak wpisu magazynowego dla produktu"));
            if (stock.getQuantity() < line.quantity()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Niewystarczający stan magazynowy: " + product.getName());
            }
            stock.setQuantity(stock.getQuantity() - line.quantity());

            OrderItem item = OrderItem.builder()
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
        return toResponse(orderRepository.findDetailById(order.getId()).orElse(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(Long userId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId).stream()
                .map(o -> orderRepository.findDetailById(o.getId()).orElse(o))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long orderId, Long currentUserId, boolean managerOrEmployee) {
        CustomerOrder order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!managerOrEmployee && !order.getCustomer().getId().equals(currentUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Brak dostępu do zamówienia");
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(o -> orderRepository.findDetailById(o.getId()).orElse(o))
                .map(this::toResponse);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        CustomerOrder order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        OrderStatus old = order.getStatus();
        if (!isAllowedTransition(old, newStatus)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Niedozwolona zmiana statusu: " + old + " -> " + newStatus);
        }
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(Instant.now());
        }
        if (newStatus == OrderStatus.IN_DELIVERY) {
            ensureDelivery(order);
        }
        if (newStatus == OrderStatus.DELIVERED) {
            deliveryRepository.findByOrderId(orderId).ifPresent(d -> {
                d.setStatus(DeliveryStatus.DELIVERED);
                d.setDeliveredAt(Instant.now());
            });
        }
        orderRepository.save(order);
        notificationPublisher.publishOrderStatusChange(order.getCustomer().getEmail(), order.getId(), newStatus);
        return toResponse(orderRepository.findDetailById(orderId).orElseThrow());
    }

    @Transactional
    public OrderResponse cancel(Long orderId, Long customerId) {
        CustomerOrder order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Brak dostępu");
        }
        if (order.getStatus() != OrderStatus.SUBMITTED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Można anulować tylko zamówienie w statusie SUBMITTED");
        }
        for (OrderItem item : order.getItems()) {
            ProductStock stock = productStockRepository.findById(item.getProduct().getId()).orElseThrow();
            stock.setQuantity(stock.getQuantity() + item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        notificationPublisher.publishOrderStatusChange(order.getCustomer().getEmail(), order.getId(), OrderStatus.CANCELLED);
        return toResponse(orderRepository.findDetailById(orderId).orElseThrow());
    }

    private void ensureDelivery(CustomerOrder order) {
        if (deliveryRepository.findByOrderId(order.getId()).isPresent()) {
            return;
        }
        Delivery d = Delivery.builder()
                .order(order)
                .status(DeliveryStatus.PENDING)
                .addressSnapshot(order.getDeliveryAddress())
                .build();
        deliveryRepository.save(d);
    }

    private boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
        if (to == OrderStatus.CANCELLED) {
            return false;
        }
        return switch (from) {
            case SUBMITTED -> to == OrderStatus.IN_PRODUCTION || to == OrderStatus.IN_PACKING;
            case IN_PRODUCTION -> to == OrderStatus.IN_PACKING || to == OrderStatus.IN_DELIVERY;
            case IN_PACKING -> to == OrderStatus.IN_DELIVERY;
            case IN_DELIVERY -> to == OrderStatus.DELIVERED;
            default -> false;
        };
    }

    private OrderResponse toResponse(CustomerOrder o) {
        List<OrderItemResponse> items = o.getItems() == null ? List.of() : o.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice()
                ))
                .toList();
        return new OrderResponse(
                o.getId(),
                o.getCustomer().getId(),
                o.getStatus(),
                o.getDeliveryAddress(),
                o.getTotalAmount(),
                o.getCreatedAt(),
                o.getDeliveredAt(),
                items
        );
    }
}
