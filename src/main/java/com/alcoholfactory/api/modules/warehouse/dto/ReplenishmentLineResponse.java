package com.alcoholfactory.api.modules.warehouse.dto;

import java.math.BigDecimal;

public record ReplenishmentLineResponse(
        Long id,
        Long productId,
        String productName,
        Long rawMaterialId,
        String rawMaterialName,
        BigDecimal quantityDelta
) {}
