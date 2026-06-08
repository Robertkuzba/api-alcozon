package com.alcoholfactory.api.modules.inventory.dto;

public record InventoryProductRow(
    Long productId, String name, int quantity, String warehouseZone) {}
