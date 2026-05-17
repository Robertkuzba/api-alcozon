package com.alcoholfactory.api.modules.delivery.api;

import com.alcoholfactory.api.modules.delivery.dto.DeliveryResponse;
import com.alcoholfactory.api.modules.delivery.dto.PatchDeliveryAssignRequest;
import com.alcoholfactory.api.modules.delivery.dto.PatchDeliveryStatusRequest;
import com.alcoholfactory.api.modules.delivery.service.DeliveryService;
import com.alcoholfactory.api.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public List<DeliveryResponse> all() {
        return deliveryService.all();
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public List<DeliveryResponse> my(@AuthenticationPrincipal AppUserDetails user) {
        return deliveryService.my(user.getId());
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Przypisanie kuriera do dostawy (tylko MANAGER / desktop)")
    public DeliveryResponse assign(@PathVariable Long id, @Valid @RequestBody PatchDeliveryAssignRequest req) {
        return deliveryService.assign(id, req.courierId());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public DeliveryResponse status(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody PatchDeliveryStatusRequest req
    ) {
        return deliveryService.patchStatus(id, req.status(), user.getId());
    }
}
