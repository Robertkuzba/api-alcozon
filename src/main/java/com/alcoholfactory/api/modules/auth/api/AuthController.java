package com.alcoholfactory.api.modules.auth.api;

import com.alcoholfactory.api.modules.auth.dto.LoginRequest;
import com.alcoholfactory.api.modules.auth.dto.RefreshRequest;
import com.alcoholfactory.api.modules.auth.dto.RegisterRequest;
import com.alcoholfactory.api.modules.auth.dto.StaffLoginRequest;
import com.alcoholfactory.api.modules.auth.dto.StaffLoginResponse;
import com.alcoholfactory.api.modules.auth.dto.TokenResponse;
import com.alcoholfactory.api.modules.auth.dto.VerifyDeviceRequest;
import com.alcoholfactory.api.modules.auth.service.AuthService;
import com.alcoholfactory.api.modules.auth.service.StaffAuthService;
import com.alcoholfactory.api.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final StaffAuthService staffAuthService;

    @PostMapping("/register")
    @Operation(summary = "Rejestracja (CUSTOMER)")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Logowanie (CUSTOMER / GUEST — nie dla staff)")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/staff/login")
    @Operation(summary = "Logowanie pracownika (EMPLOYEE/MANAGER) — 2FA e-mail + deviceId")
    public StaffLoginResponse staffLogin(@Valid @RequestBody StaffLoginRequest request) {
        return staffAuthService.staffLogin(request);
    }

    @PostMapping("/staff/verify-device")
    @Operation(summary = "Potwierdzenie kodu 4-cyfrowego z e-maila (po staff/login)")
    public TokenResponse verifyDevice(@Valid @RequestBody VerifyDeviceRequest request) {
        return staffAuthService.verifyDevice(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Odświeżenie access tokena")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Wylogowanie (unieważnienie refresh tokena)")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/guest")
    @Operation(summary = "Sesja gościa (JWT z rolą GUEST)")
    public TokenResponse guest() {
        return authService.createGuestSession();
    }

    @PostMapping("/confirm-age")
    @PreAuthorize("hasRole('GUEST')")
    @Operation(summary = "Potwierdzenie 18+ (GUEST → CUSTOMER), zwraca nowe tokeny")
    public TokenResponse confirmAge(@AuthenticationPrincipal AppUserDetails user) {
        return authService.confirmAgeAndReissue(user);
    }
}
