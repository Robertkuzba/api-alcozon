package com.alcoholfactory.api.modules.inventory.api;

import com.alcoholfactory.api.modules.inventory.dto.InventoryOverviewResponse;
import com.alcoholfactory.api.modules.inventory.dto.InventoryProductRow;
import com.alcoholfactory.api.modules.inventory.dto.InventoryRawRow;
import com.alcoholfactory.api.modules.inventory.dto.PatchQuantityRequest;
import com.alcoholfactory.api.modules.inventory.dto.PatchRawQuantityRequest;
import com.alcoholfactory.api.modules.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public InventoryOverviewResponse get() {
        return inventoryService.overview();
    }

    @PatchMapping("/products/{productId}")
    public InventoryProductRow patchProduct(
            @PathVariable Long productId,
            @Valid @RequestBody PatchQuantityRequest req
    ) {
        return inventoryService.patchProductStock(productId, req.delta());
    }

    @PatchMapping("/raw-materials/{rawMaterialId}")
    public InventoryRawRow patchRaw(
            @PathVariable Long rawMaterialId,
            @Valid @RequestBody PatchRawQuantityRequest body
    ) {
        return inventoryService.patchRawMaterial(rawMaterialId, body.delta());
    }
}
