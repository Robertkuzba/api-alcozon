package com.alcoholfactory.api.modules.order.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** Lista magazynu: stronicowane zamówienia sklepu + wszystkie custom (staff). */
public record StaffCombinedOrdersResponse(
    Page<OrderResponse> shopOrders, List<CustomOrderResponse> customOrders) {}
