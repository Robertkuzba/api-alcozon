package com.alcoholfactory.api.modules.inventory.dto;

import java.math.BigDecimal;

public record InventoryRawRow(Long id, String name, String unit, BigDecimal quantity) {}
