package com.alcoholfactory.api.modules.order.dto;

import java.util.List;

/**
 * Sklep + zamówienia własne w jednej odpowiedzi (mobilka / desktop).
 */
public record CombinedOrdersResponse(
        List<OrderResponse> shopOrders,
        List<CustomOrderResponse> customOrders
) {}
