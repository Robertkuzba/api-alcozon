package com.alcoholfactory.api.modules.inventory.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PatchRawQuantityRequest(@NotNull BigDecimal delta) {}
