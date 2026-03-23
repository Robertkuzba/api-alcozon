package com.alcoholfactory.api.modules.delivery.service;

import com.alcoholfactory.api.common.domain.DeliveryStatus;
import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.delivery.dto.DeliveryResponse;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
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
    private final com.alcoholfactory.api.notification.OrderNotificationPublisher notificationPublisher;

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
            var order = d.getOrder();
            order.setStatus(OrderStatus.DELIVERED);
            order.setDeliveredAt(Instant.now());
            orderRepository.save(order);
            notificationPublisher.publishOrderStatusChange(
                    order.getCustomer().getEmail(),
                    order.getId(),
                    OrderStatus.DELIVERED
            );
        }
        deliveryRepository.save(d);
        return toResponse(d);
    }

    private DeliveryResponse toResponse(Delivery d) {
        return new DeliveryResponse(
                d.getId(),
                d.getOrder().getId(),
                d.getCourier() != null ? d.getCourier().getId() : null,
                d.getCourier() != null ? d.getCourier().getEmail() : null,
                d.getStatus(),
                d.getAddressSnapshot(),
                d.getOrder().getCustomer().getEmail(),
                d.getStartedAt(),
                d.getDeliveredAt()
        );
    }
}
