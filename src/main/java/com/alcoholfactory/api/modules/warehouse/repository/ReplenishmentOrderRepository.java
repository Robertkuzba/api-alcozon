package com.alcoholfactory.api.modules.warehouse.repository;

import com.alcoholfactory.api.modules.warehouse.domain.ReplenishmentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplenishmentOrderRepository extends JpaRepository<ReplenishmentOrder, Long> {

    List<ReplenishmentOrder> findAllByOrderByCreatedAtDesc();
}
