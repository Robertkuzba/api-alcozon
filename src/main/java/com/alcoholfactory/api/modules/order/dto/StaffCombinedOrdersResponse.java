package com.alcoholfactory.api.modules.order.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Lista magazynu: stronicowane zamówienia sklepu + wszystkie custom (staff).
 */
public record StaffCombinedOrdersResponse(
        Page<OrderResponse> shopOrders,
        List<CustomOrderResponse> customOrders
) {}
