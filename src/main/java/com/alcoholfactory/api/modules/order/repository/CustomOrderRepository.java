package com.alcoholfactory.api.modules.order.repository;

import com.alcoholfactory.api.common.domain.CustomOrderStatus;
import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {

    List<CustomOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<CustomOrder> findByStatusOrderByCreatedAtDesc(CustomOrderStatus status);

    @Query("SELECT DISTINCT c FROM CustomOrder c JOIN FETCH c.customer LEFT JOIN FETCH c.assignedTo ORDER BY c.createdAt DESC")
    List<CustomOrder> findAllWithUsers();

    @Query("SELECT c FROM CustomOrder c JOIN FETCH c.customer LEFT JOIN FETCH c.assignedTo WHERE c.id = :id")
    Optional<CustomOrder> findFetchedById(@Param("id") Long id);

    boolean existsByClientOrderNumber(String clientOrderNumber);

    @Query("SELECT c FROM CustomOrder c JOIN FETCH c.customer WHERE c.clientOrderNumber = :clientOrderNumber")
    Optional<CustomOrder> findByClientOrderNumberWithCustomer(@Param("clientOrderNumber") String clientOrderNumber);
}
