package com.alcoholfactory.api.modules.warehouse.repository;

import com.alcoholfactory.api.modules.warehouse.domain.ReplenishmentOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplenishmentOrderRepository extends JpaRepository<ReplenishmentOrder, Long> {

  @EntityGraph(attributePaths = {"manager", "lines", "lines.product", "lines.rawMaterial"})
  List<ReplenishmentOrder> findAllByOrderByCreatedAtDesc();

  @EntityGraph(attributePaths = {"manager", "lines", "lines.product", "lines.rawMaterial"})
  Optional<ReplenishmentOrder> findWithDetailsById(Long id);
}
