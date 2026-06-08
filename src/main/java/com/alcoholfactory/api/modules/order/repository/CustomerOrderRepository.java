package com.alcoholfactory.api.modules.order.repository;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

  List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

  boolean existsByClientOrderNumber(String clientOrderNumber);

  @Query(
      "SELECT o FROM CustomerOrder o JOIN FETCH o.customer WHERE o.clientOrderNumber ="
          + " :clientOrderNumber")
  Optional<CustomerOrder> findByClientOrderNumberWithCustomer(
      @Param("clientOrderNumber") String clientOrderNumber);

  @Query("SELECT o FROM CustomerOrder o JOIN FETCH o.customer WHERE o.id = :id")
  Optional<CustomerOrder> findByIdWithCustomer(@Param("id") Long id);

  @Query(
      "SELECT DISTINCT o FROM CustomerOrder o JOIN FETCH o.customer LEFT JOIN FETCH o.items i LEFT"
          + " JOIN FETCH i.product WHERE o.id = :id")
  Optional<CustomerOrder> findDetailById(@Param("id") Long id);

  List<CustomerOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

  @Query(
      """
      SELECT DISTINCT o FROM CustomerOrder o
      JOIN FETCH o.customer
      JOIN com.alcoholfactory.api.modules.delivery.domain.Delivery d ON d.order = o
      WHERE d.courier.id = :courierId AND o.status = :status
      ORDER BY o.createdAt DESC
      """)
  List<CustomerOrder> findInDeliveryAssignedToCourier(
      @Param("courierId") Long courierId, @Param("status") OrderStatus status);

  @Query(
      """
      SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerOrder o
      WHERE o.status IN :statuses AND o.createdAt >= :from AND o.createdAt <= :to
      """)
  BigDecimal sumTotalForStatusesBetween(
      @Param("from") Instant from,
      @Param("to") Instant to,
      @Param("statuses") List<OrderStatus> statuses);
}
