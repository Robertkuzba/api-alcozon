package com.alcoholfactory.api.modules.order.api;

import com.alcoholfactory.api.modules.order.dto.CreateCustomOrderRequest;
import com.alcoholfactory.api.modules.order.dto.CustomOrderResponse;
import com.alcoholfactory.api.modules.order.dto.PatchAssignRequest;
import com.alcoholfactory.api.modules.order.dto.PatchCustomOrderStatusRequest;
import com.alcoholfactory.api.modules.order.service.CustomOrderService;
import com.alcoholfactory.api.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/custom-orders")
@RequiredArgsConstructor
@Tag(name = "Custom orders")
public class CustomOrderController {

    private static final Set<String> STAFF = Set.of("ROLE_EMPLOYEE", "ROLE_MANAGER");

    private final CustomOrderService customOrderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomOrderResponse create(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody CreateCustomOrderRequest request
    ) {
        return customOrderService.create(user.getId(), request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<CustomOrderResponse> my(@AuthenticationPrincipal AppUserDetails user) {
        return customOrderService.my(user.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    @Operation(summary = "Lista zapytań niestandardowych")
    public List<CustomOrderResponse> list() {
        return customOrderService.listForStaff();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CustomOrderResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {
        boolean staff = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(STAFF::contains);
        return customOrderService.get(id, user.getId(), staff);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public CustomOrderResponse patchStatus(@PathVariable Long id, @Valid @RequestBody PatchCustomOrderStatusRequest req) {
        return customOrderService.patchStatus(id, req.status());
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public CustomOrderResponse assign(@PathVariable Long id, @Valid @RequestBody PatchAssignRequest req) {
        return customOrderService.assign(id, req.assigneeUserId());
    }
}
