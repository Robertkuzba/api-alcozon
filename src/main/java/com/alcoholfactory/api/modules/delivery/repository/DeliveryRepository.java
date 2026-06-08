package com.alcoholfactory.api.modules.delivery.repository;

import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

  Optional<Delivery> findByOrderId(Long orderId);

  Optional<Delivery> findByCustomOrderId(Long customOrderId);

  List<Delivery> findByCourierIdOrderByIdDesc(Long courierId);

  @Query(
      """
      SELECT d FROM Delivery d
      LEFT JOIN FETCH d.order o
      LEFT JOIN FETCH o.customer
      LEFT JOIN FETCH d.customOrder co
      LEFT JOIN FETCH co.customer
      LEFT JOIN FETCH d.courier
      ORDER BY d.id DESC
      """)
  List<Delivery> findAllFetched();

  @Query(
      """
      SELECT d FROM Delivery d
      LEFT JOIN FETCH d.order o
      LEFT JOIN FETCH o.customer
      LEFT JOIN FETCH d.customOrder co
      LEFT JOIN FETCH co.customer
      LEFT JOIN FETCH d.courier
      WHERE d.courier.id = :courierId
      ORDER BY d.id DESC
      """)
  List<Delivery> findByCourierIdFetched(@Param("courierId") Long courierId);

  @Query(
      """
      SELECT d FROM Delivery d
      LEFT JOIN FETCH d.order o
      LEFT JOIN FETCH o.customer
      LEFT JOIN FETCH d.customOrder co
      LEFT JOIN FETCH co.customer
      LEFT JOIN FETCH d.courier
      WHERE d.id = :id
      """)
  Optional<Delivery> findFetchedById(@Param("id") Long id);
}
