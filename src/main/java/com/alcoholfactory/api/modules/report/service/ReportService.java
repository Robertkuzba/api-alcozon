package com.alcoholfactory.api.modules.report.service;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.modules.inventory.dto.InventoryOverviewResponse;
import com.alcoholfactory.api.modules.inventory.service.InventoryService;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final CustomerOrderRepository orderRepository;
  private final InventoryService inventoryService;

  private static final List<OrderStatus> SALES_STATUSES =
      List.of(
          OrderStatus.SUBMITTED,
          OrderStatus.IN_PRODUCTION,
          OrderStatus.IN_PACKING,
          OrderStatus.IN_DELIVERY,
          OrderStatus.DELIVERED);

  @Transactional(readOnly = true)
  public BigDecimal salesTotal(Instant from, Instant to) {
    BigDecimal sum = orderRepository.sumTotalForStatusesBetween(from, to, SALES_STATUSES);
    return sum != null ? sum : BigDecimal.ZERO;
  }

  @Transactional(readOnly = true)
  public InventoryOverviewResponse inventorySnapshot() {
    return inventoryService.overview();
  }
}
