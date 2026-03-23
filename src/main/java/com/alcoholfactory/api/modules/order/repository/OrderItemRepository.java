package com.alcoholfactory.api.modules.order.repository;

import com.alcoholfactory.api.modules.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
