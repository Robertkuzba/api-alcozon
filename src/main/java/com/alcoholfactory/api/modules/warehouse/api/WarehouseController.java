package com.alcoholfactory.api.modules.warehouse.api;

import com.alcoholfactory.api.modules.warehouse.dto.CreateReplenishmentRequest;
import com.alcoholfactory.api.modules.warehouse.dto.ReplenishmentOrderResponse;
import com.alcoholfactory.api.modules.warehouse.service.WarehouseService;
import com.alcoholfactory.api.security.AppUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/replenishment")
    @ResponseStatus(HttpStatus.CREATED)
    public ReplenishmentOrderResponse create(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody CreateReplenishmentRequest request
    ) {
        return warehouseService.create(user.getId(), request);
    }

    @GetMapping("/replenishment")
    public List<ReplenishmentOrderResponse> history() {
        return warehouseService.history();
    }
}
