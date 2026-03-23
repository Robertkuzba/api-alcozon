package com.alcoholfactory.api.modules.auth.service;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.auth.domain.RefreshToken;
import com.alcoholfactory.api.modules.auth.dto.LoginRequest;
import com.alcoholfactory.api.modules.auth.dto.RefreshRequest;
import com.alcoholfactory.api.modules.auth.dto.RegisterRequest;
import com.alcoholfactory.api.modules.auth.dto.TokenResponse;
import com.alcoholfactory.api.modules.auth.repository.RefreshTokenRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.security.AppUserDetails;
import com.alcoholfactory.api.security.JwtService;
import com.alcoholfactory.api.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = User.builder()
                .email(req.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(UserRole.CUSTOMER)
                .active(true)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .courier(false)
                .build();
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.isActive() || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        String hash = TokenHasher.sha256Hex(req.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        User user = stored.getUser();
        if (!user.isActive()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "User inactive");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshRequest req) {
        String hash = TokenHasher.sha256Hex(req.refreshToken());
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public TokenResponse createGuestSession() {
        String email = "guest-" + UUID.randomUUID() + "@guest.local";
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(UserRole.GUEST)
                .active(true)
                .courier(false)
                .build();
        userRepository.save(user);
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse confirmAgeAndReissue(AppUserDetails principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != UserRole.GUEST) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only guests can confirm age this way");
        }
        user.setRole(UserRole.CUSTOMER);
        user.setAgeConfirmedAt(Instant.now());
        userRepository.save(user);
        return issueTokens(user);
    }

    private TokenResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshPlain = jwtService.generateRefreshTokenValue();
        String hash = TokenHasher.sha256Hex(refreshPlain);
        Instant exp = Instant.now().plusSeconds(jwtService.refreshTtlSeconds());
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(exp)
                .revoked(false)
                .build());
        return TokenResponse.of(access, refreshPlain, jwtService.accessTtlSeconds());
    }
}
