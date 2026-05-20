package com.alcoholfactory.api.modules.order.service;

import com.alcoholfactory.api.common.domain.DeliveryStatus;
import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import com.alcoholfactory.api.modules.order.dto.CreateCustomOrderRequest;
import com.alcoholfactory.api.modules.order.dto.CustomOrderResponse;
import com.alcoholfactory.api.modules.order.dto.CustomOrderTrackResponse;
import com.alcoholfactory.api.modules.order.repository.CustomOrderRepository;
import com.alcoholfactory.api.modules.order.util.CustomOrderDeliveryDetailsResolver;
import com.alcoholfactory.api.modules.order.util.OrderNumbers;
import com.alcoholfactory.api.modules.order.util.OrderStatusTransitions;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.notification.CustomOrderRealtimeNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private static final String PREF_CLIENT_ORDER_NUMBER = "clientOrderNumber";

    private final CustomOrderRepository customOrderRepository;
    private final UserRepository userRepository;
    private final DeliveryRepository deliveryRepository;
    private final CustomOrderRealtimeNotifier customOrderRealtimeNotifier;

    @Transactional
    public CustomOrderResponse create(Long userId, CreateCustomOrderRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != UserRole.CUSTOMER || user.getAgeConfirmedAt() == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Wymagane CUSTOMER z potwierdzonym wiekiem");
        }

        String clientOrderNumber = resolveClientOrderNumber(req.clientOrderNumber(), req.preferences());
        if (clientOrderNumber != null && customOrderRepository.existsByClientOrderNumber(clientOrderNumber)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Numer zamówienia jest już użyty");
        }

        CustomOrder co = CustomOrder.builder()
                .customer(user)
                .description(req.description())
                .clientOrderNumber(clientOrderNumber)
                .preferences(req.preferences())
                .status(OrderStatus.SUBMITTED)
                .build();
        customOrderRepository.save(co);
        customOrderRealtimeNotifier.onCustomOrderCreated(co);
        return toResponse(co);
    }

    @Transactional(readOnly = true)
    public CustomOrderTrackResponse trackPublic(String orderRef, String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid email");
        }
        CustomOrder order = resolveCustomOrder(orderRef);
        if (!order.getCustomer().getEmail().equalsIgnoreCase(normalized)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return new CustomOrderTrackResponse(
                order.getId(),
                displayClientOrderNumber(order),
                order.getStatus(),
                order.getDescription(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<CustomOrderResponse> my(Long userId) {
        return customOrderRepository.findByCustomerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomOrderResponse> listForStaff() {
        return customOrderRepository.findAllWithUsers().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomOrderResponse get(Long id, Long currentUserId, boolean staff) {
        CustomOrder co = customOrderRepository.findFetchedById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        if (!staff && !co.getCustomer().getId().equals(currentUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Brak dostępu");
        }
        return toResponse(co);
    }

    @Transactional
    public CustomOrderResponse patchStatus(Long id, OrderStatus newStatus) {
        CustomOrder co = customOrderRepository.findFetchedById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        OrderStatus old = co.getStatus();
        if (!OrderStatusTransitions.isAllowed(old, newStatus)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Niedozwolona zmiana statusu: " + old + " -> " + newStatus);
        }
        co.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            co.setDeliveredAt(Instant.now());
        }
        if (newStatus == OrderStatus.IN_DELIVERY) {
            ensureDelivery(co);
        }
        if (newStatus == OrderStatus.DELIVERED) {
            deliveryRepository.findByCustomOrderId(co.getId()).ifPresent(d -> {
                d.setStatus(DeliveryStatus.DELIVERED);
                d.setDeliveredAt(Instant.now());
                deliveryRepository.save(d);
            });
        }
        customOrderRepository.save(co);
        customOrderRealtimeNotifier.onCustomOrderStatusChanged(co, newStatus);
        return toResponse(co);
    }

    @Transactional
    public CustomOrderResponse assign(Long id, Long assigneeId) {
        CustomOrder co = customOrderRepository.findFetchedById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Invalid assignee"));
        co.setAssignedTo(assignee);
        return toResponse(customOrderRepository.save(co));
    }

    private void ensureDelivery(CustomOrder order) {
        if (deliveryRepository.findByCustomOrderId(order.getId()).isPresent()) {
            return;
        }
        Delivery delivery = Delivery.builder()
                .customOrder(order)
                .clientOrderNumber(displayClientOrderNumber(order))
                .status(DeliveryStatus.PENDING)
                .deliveryDetails(CustomOrderDeliveryDetailsResolver.fromPreferences(order))
                .build();
        deliveryRepository.save(delivery);
    }

    private CustomOrder resolveCustomOrder(String orderRef) {
        String raw = orderRef.trim();
        if (raw.regionMatches(true, 0, "CUSTOM-", 0, "CUSTOM-".length())) {
            raw = raw.substring("CUSTOM-".length());
        }
        Long id = OrderNumbers.parseId(raw);
        if (id != null) {
            var byId = customOrderRepository.findFetchedById(id);
            if (byId.isPresent()) {
                return byId.get();
            }
        }
        return customOrderRepository.findByClientOrderNumberWithCustomer(raw)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private static String resolveClientOrderNumber(String explicit, Map<String, Object> preferences) {
        if (StringUtils.hasText(explicit)) {
            return explicit.trim();
        }
        if (preferences == null) {
            return null;
        }
        Object raw = preferences.get(PREF_CLIENT_ORDER_NUMBER);
        if (raw == null) {
            return null;
        }
        String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static String displayClientOrderNumber(CustomOrder co) {
        if (StringUtils.hasText(co.getClientOrderNumber())) {
            return co.getClientOrderNumber();
        }
        return "CUSTOM-" + co.getId();
    }

    public CustomOrderResponse toResponse(CustomOrder co) {
        return new CustomOrderResponse(
                co.getId(),
                displayClientOrderNumber(co),
                co.getCustomer().getId(),
                co.getDescription(),
                co.getPreferences(),
                co.getStatus(),
                co.getDeliveredAt(),
                co.getAssignedTo() != null ? co.getAssignedTo().getId() : null,
                co.getCreatedAt(),
                co.getUpdatedAt()
        );
    }
}
