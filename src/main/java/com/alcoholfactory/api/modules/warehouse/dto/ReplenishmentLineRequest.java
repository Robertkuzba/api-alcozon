package com.alcoholfactory.api.modules.warehouse.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReplenishmentLineRequest(
    Long productId, Long rawMaterialId, @NotNull BigDecimal quantityDelta) {}
