package com.alcoholfactory.api.modules.order.repository;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {

  List<CustomOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

  List<CustomOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

  @Query(
      "SELECT DISTINCT c FROM CustomOrder c JOIN FETCH c.customer LEFT JOIN FETCH c.assignedTo"
          + " ORDER BY c.createdAt DESC")
  List<CustomOrder> findAllWithUsers();

  @Query(
      "SELECT c FROM CustomOrder c JOIN FETCH c.customer LEFT JOIN FETCH c.assignedTo WHERE c.id ="
          + " :id")
  Optional<CustomOrder> findFetchedById(@Param("id") Long id);

  boolean existsByClientOrderNumber(String clientOrderNumber);

  @Query(
      "SELECT c FROM CustomOrder c JOIN FETCH c.customer WHERE c.clientOrderNumber ="
          + " :clientOrderNumber")
  Optional<CustomOrder> findByClientOrderNumberWithCustomer(
      @Param("clientOrderNumber") String clientOrderNumber);

  @Query(
      """
      SELECT DISTINCT c FROM CustomOrder c
      JOIN FETCH c.customer
      JOIN com.alcoholfactory.api.modules.delivery.domain.Delivery d ON d.customOrder = c
      WHERE d.courier.id = :courierId AND c.status = :status
      ORDER BY c.createdAt DESC
      """)
  List<CustomOrder> findInDeliveryAssignedToCourier(
      @Param("courierId") Long courierId, @Param("status") OrderStatus status);
}
