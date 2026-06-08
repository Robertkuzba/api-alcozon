package com.alcoholfactory.api.modules.inventory.dto;

import java.util.List;

public record InventoryOverviewResponse(
    List<InventoryProductRow> products, List<InventoryRawRow> rawMaterials) {}
