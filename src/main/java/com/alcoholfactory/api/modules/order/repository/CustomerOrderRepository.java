package com.alcoholfactory.api.modules.order.repository;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT o FROM CustomerOrder o JOIN FETCH o.customer WHERE o.id = :id")
    Optional<CustomerOrder> findByIdWithCustomer(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM CustomerOrder o JOIN FETCH o.customer LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<CustomerOrder> findDetailById(@Param("id") Long id);

    List<CustomerOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerOrder o
            WHERE o.status IN :statuses AND o.createdAt >= :from AND o.createdAt <= :to
            """)
    BigDecimal sumTotalForStatusesBetween(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("statuses") List<OrderStatus> statuses
    );
}
