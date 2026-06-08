package com.alcoholfactory.api.modules.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long productId, String productName, int quantity, BigDecimal unitPrice) {}
