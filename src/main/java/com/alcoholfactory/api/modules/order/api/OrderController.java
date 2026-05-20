package com.alcoholfactory.api.modules.order.api;

import com.alcoholfactory.api.modules.order.dto.CombinedOrdersResponse;
import com.alcoholfactory.api.modules.order.dto.CreateOrderRequest;
import com.alcoholfactory.api.modules.order.dto.OrderResponse;
import com.alcoholfactory.api.modules.order.dto.OrderTrackResponse;
import com.alcoholfactory.api.modules.order.dto.PatchOrderStatusRequest;
import com.alcoholfactory.api.modules.order.dto.StaffCombinedOrdersResponse;
import com.alcoholfactory.api.modules.order.service.CombinedOrderQueryService;
import com.alcoholfactory.api.modules.order.service.OrderService;
import com.alcoholfactory.api.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orders")
public class OrderController {

    private static final Set<String> STAFF_ROLES = Set.of("ROLE_EMPLOYEE", "ROLE_MANAGER");

    private final OrderService orderService;
    private final CombinedOrderQueryService combinedOrderQueryService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Złożenie zamówienia (CUSTOMER + 18+; pole delivery ze strukturą adresu)")
    public OrderResponse create(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return orderService.create(user.getId(), request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Moje zamówienia")
    public List<OrderResponse> my(@AuthenticationPrincipal AppUserDetails user) {
        return orderService.myOrders(user.getId());
    }

    @GetMapping("/track")
    @Operation(summary = "Publiczne śledzenie (id, ORD-{id} lub clientOrderNumber + e-mail, bez JWT)")
    public OrderTrackResponse trackPublic(
            @RequestParam @NotBlank String orderId,
            @RequestParam @NotBlank @Email String email
    ) {
        return orderService.trackPublic(orderId, email);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    @Operation(summary = "Lista zamówień sklepu (pracownik / manager, stronicowanie)")
    public Page<OrderResponse> list(Pageable pageable) {
        return orderService.listAll(pageable);
    }

    @GetMapping("/staff/combined")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    @Operation(summary = "Lista magazynu: zamówienia sklepu (page) + custom w jednym żądaniu")
    public StaffCombinedOrdersResponse staffCombined(Pageable pageable) {
        return combinedOrderQueryService.listForStaff(pageable);
    }

    @GetMapping("/for-courier/{courierUserId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    @Operation(summary = "IN_DELIVERY przypisane do kuriera: sklep + custom w jednym żądaniu")
    public CombinedOrdersResponse forCourier(
            @PathVariable @Positive Long courierUserId,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return combinedOrderQueryService.forCourier(courierUserId, user.getId(), isStaff(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Szczegóły zamówienia (id, ORD-{id} lub clientOrderNumber)")
    public OrderResponse get(
            @PathVariable String id,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        boolean staff = isStaff(user);
        return orderService.getByRef(id, user.getId(), staff);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    @Operation(summary = "Zmiana statusu zamówienia")
    public OrderResponse patchStatus(@PathVariable Long id, @Valid @RequestBody PatchOrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Anulowanie zamówienia (tylko SUBMITTED)")
    public OrderResponse cancel(@PathVariable String id, @AuthenticationPrincipal AppUserDetails user) {
        return orderService.cancel(id, user.getId());
    }

    private static boolean isStaff(AppUserDetails user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(STAFF_ROLES::contains);
    }
}
