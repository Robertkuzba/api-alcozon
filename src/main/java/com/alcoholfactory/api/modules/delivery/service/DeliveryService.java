package com.alcoholfactory.api.modules.delivery.service;

import com.alcoholfactory.api.common.domain.DeliveryStatus;
import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.delivery.dto.DeliveryResponse;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import com.alcoholfactory.api.modules.order.dto.OrderDeliveryDetailsResponse;
import com.alcoholfactory.api.modules.order.repository.CustomOrderRepository;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.notification.CustomOrderRealtimeNotifier;
import com.alcoholfactory.api.notification.OrderRealtimeNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final CustomerOrderRepository orderRepository;
    private final CustomOrderRepository customOrderRepository;
    private final OrderRealtimeNotifier orderRealtimeNotifier;
    private final CustomOrderRealtimeNotifier customOrderRealtimeNotifier;

    @Transactional(readOnly = true)
    public List<DeliveryResponse> all() {
        return deliveryRepository.findAllFetched().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> my(Long courierId) {
        return deliveryRepository.findByCourierIdFetched(courierId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public DeliveryResponse assign(Long deliveryId, Long courierId) {
        Delivery d = deliveryRepository.findFetchedById(deliveryId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Delivery not found"));
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Invalid courier"));
        d.setCourier(courier);
        d.setStatus(DeliveryStatus.ASSIGNED);
        deliveryRepository.save(d);
        if (d.getCustomOrder() != null) {
            customOrderRealtimeNotifier.onCustomDeliveryAssigned(d);
        } else {
            orderRealtimeNotifier.onDeliveryAssigned(d);
        }
        return toResponse(d);
    }

    @Transactional
    public DeliveryResponse patchStatus(Long deliveryId, DeliveryStatus status, Long actorId) {
        Delivery d = deliveryRepository.findFetchedById(deliveryId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Delivery not found"));
        User actor = userRepository.findById(actorId).orElseThrow();
        boolean manager = actor.getRole() == UserRole.MANAGER;
        boolean sameCourier = d.getCourier() != null && d.getCourier().getId().equals(actorId);
        if (!manager && !sameCourier) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only assigned courier or manager");
        }
        d.setStatus(status);
        if (status == DeliveryStatus.IN_TRANSIT && d.getStartedAt() == null) {
            d.setStartedAt(Instant.now());
        }
        if (status == DeliveryStatus.DELIVERED) {
            d.setDeliveredAt(Instant.now());
            if (d.getCustomOrder() != null) {
                CustomOrder custom = d.getCustomOrder();
                custom.setStatus(OrderStatus.DELIVERED);
                custom.setDeliveredAt(Instant.now());
                customOrderRepository.save(custom);
                customOrderRealtimeNotifier.onCustomOrderDelivered(custom, d);
            } else {
                CustomerOrder order = d.getOrder();
                order.setStatus(OrderStatus.DELIVERED);
                order.setDeliveredAt(Instant.now());
                orderRepository.save(order);
                orderRealtimeNotifier.onOrderDelivered(order, d);
            }
        }
        deliveryRepository.save(d);
        return toResponse(d);
    }

    private DeliveryResponse toResponse(Delivery d) {
        boolean custom = d.getCustomOrder() != null;
        String clientOrderNumber = d.getClientOrderNumber();
        if (clientOrderNumber == null || clientOrderNumber.isBlank()) {
            if (custom) {
                CustomOrder co = d.getCustomOrder();
                clientOrderNumber = co.getClientOrderNumber() != null
                        ? co.getClientOrderNumber()
                        : "CUSTOM-" + co.getId();
            } else {
                clientOrderNumber = d.getOrder().getClientOrderNumber();
            }
        }
        String customerEmail = custom
                ? d.getCustomOrder().getCustomer().getEmail()
                : d.getOrder().getCustomer().getEmail();
        Long orderId = custom ? null : d.getOrder().getId();
        Long customOrderId = custom ? d.getCustomOrder().getId() : null;

        return new DeliveryResponse(
                d.getId(),
                orderId,
                customOrderId,
                custom,
                clientOrderNumber,
                d.getCourier() != null ? d.getCourier().getId() : null,
                d.getCourier() != null ? d.getCourier().getEmail() : null,
                d.getStatus(),
                OrderDeliveryDetailsResponse.from(d.getDeliveryDetails()),
                customerEmail,
                d.getStartedAt(),
                d.getDeliveredAt()
        );
    }
}
