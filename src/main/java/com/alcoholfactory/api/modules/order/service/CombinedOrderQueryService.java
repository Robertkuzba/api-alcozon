package com.alcoholfactory.api.modules.order.service;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.order.dto.CombinedOrdersResponse;
import com.alcoholfactory.api.modules.order.dto.CustomOrderResponse;
import com.alcoholfactory.api.modules.order.dto.OrderResponse;
import com.alcoholfactory.api.modules.order.dto.StaffCombinedOrdersResponse;
import com.alcoholfactory.api.modules.order.repository.CustomOrderRepository;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CombinedOrderQueryService {

  private final CustomerOrderRepository orderRepository;
  private final CustomOrderRepository customOrderRepository;
  private final UserRepository userRepository;
  private final OrderService orderService;
  private final CustomOrderService customOrderService;

  @Transactional(readOnly = true)
  public StaffCombinedOrdersResponse listForStaff(Pageable pageable) {
    Page<OrderResponse> shop =
        orderRepository
            .findAll(pageable)
            .map(o -> orderRepository.findDetailById(o.getId()).orElse(o))
            .map(orderService::toResponse);
    List<CustomOrderResponse> custom =
        customOrderRepository.findAllWithUsers().stream()
            .map(customOrderService::toResponse)
            .toList();
    return new StaffCombinedOrdersResponse(shop, custom);
  }

  @Transactional(readOnly = true)
  public CombinedOrdersResponse forCourier(Long courierUserId, Long actorUserId, boolean manager) {
    validateCourierAccess(courierUserId, actorUserId, manager);
    List<OrderResponse> shop =
        orderRepository
            .findInDeliveryAssignedToCourier(courierUserId, OrderStatus.IN_DELIVERY)
            .stream()
            .map(o -> orderRepository.findDetailById(o.getId()).orElse(o))
            .map(orderService::toResponse)
            .toList();
    List<CustomOrderResponse> custom =
        customOrderRepository
            .findInDeliveryAssignedToCourier(courierUserId, OrderStatus.IN_DELIVERY)
            .stream()
            .map(customOrderService::toResponse)
            .toList();
    return new CombinedOrdersResponse(shop, custom);
  }

  private void validateCourierAccess(Long courierUserId, Long actorUserId, boolean manager) {
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
  }
}
