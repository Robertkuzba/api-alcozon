package com.alcoholfactory.api.modules.auth.service;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.config.TwoFactorProperties;
import com.alcoholfactory.api.modules.auth.domain.DeviceVerificationChallenge;
import com.alcoholfactory.api.modules.auth.domain.TrustedDevice;
import com.alcoholfactory.api.modules.auth.dto.StaffLoginRequest;
import com.alcoholfactory.api.modules.auth.dto.StaffLoginResponse;
import com.alcoholfactory.api.modules.auth.dto.TokenResponse;
import com.alcoholfactory.api.modules.auth.dto.VerifyDeviceRequest;
import com.alcoholfactory.api.modules.auth.repository.DeviceVerificationChallengeRepository;
import com.alcoholfactory.api.modules.auth.repository.TrustedDeviceRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StaffAuthService {

    private static final Set<UserRole> STAFF_ROLES = EnumSet.of(UserRole.EMPLOYEE, UserRole.MANAGER);

    private final UserRepository userRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;
    private final DeviceVerificationChallengeRepository challengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;
    private final StaffVerificationMailService mailService;
    private final TwoFactorProperties twoFactorProperties;

    @Transactional
    public StaffLoginResponse staffLogin(StaffLoginRequest req) {
        User user = authenticateStaff(req.email(), req.password());
        String deviceId = normalizeDeviceId(req.deviceId());

        if (!twoFactorProperties.enabled()) {
            return StaffLoginResponse.verified(authService.issueTokensForUser(user));
        }

        var trusted = trustedDeviceRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
        if (trusted.isPresent()) {
            TrustedDevice device = trusted.get();
            device.setLastUsedAt(Instant.now());
            trustedDeviceRepository.save(device);
            return StaffLoginResponse.verified(authService.issueTokensForUser(user));
        }

        return startVerificationChallenge(user, deviceId);
    }

    @Transactional
    public TokenResponse verifyDevice(VerifyDeviceRequest req) {
        if (!twoFactorProperties.enabled()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Two-factor authentication is disabled");
        }

        String deviceId = normalizeDeviceId(req.deviceId());
        DeviceVerificationChallenge challenge = challengeRepository.findByIdAndConsumedAtIsNull(req.challengeId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid or expired challenge"));

        if (!challenge.isActive()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid or expired challenge");
        }
        if (!challenge.getDeviceId().equals(deviceId)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid or expired challenge");
        }
        if (!verificationCodeService.matches(req.code(), challenge.getCodeHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid verification code");
        }

        challenge.setConsumedAt(Instant.now());
        challengeRepository.save(challenge);

        User user = challenge.getUser();
        trustDevice(user, deviceId);
        return authService.issueTokensForUser(user);
    }

    private User authenticateStaff(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!STAFF_ROLES.contains(user.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Staff login only for EMPLOYEE and MANAGER");
        }
        if (!user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return user;
    }

    private StaffLoginResponse startVerificationChallenge(User user, String deviceId) {
        challengeRepository.invalidatePendingForDevice(user.getId(), deviceId, Instant.now());

        String plainCode = verificationCodeService.generatePlainCode();
        Instant expiresAt = Instant.now().plusSeconds(twoFactorProperties.codeTtlSeconds());

        DeviceVerificationChallenge challenge = DeviceVerificationChallenge.builder()
                .user(user)
                .deviceId(deviceId)
                .codeHash(verificationCodeService.hashCode(plainCode))
                .expiresAt(expiresAt)
                .build();
        challengeRepository.save(challenge);

        mailService.sendVerificationCode(user.getEmail(), plainCode);
        return StaffLoginResponse.pending(challenge.getId(), twoFactorProperties.codeTtlSeconds());
    }

    private void trustDevice(User user, String deviceId) {
        Instant now = Instant.now();
        trustedDeviceRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
                .ifPresentOrElse(
                        existing -> {
                            existing.setLastUsedAt(now);
                            trustedDeviceRepository.save(existing);
                        },
                        () -> trustedDeviceRepository.save(TrustedDevice.builder()
                                .user(user)
                                .deviceId(deviceId)
                                .lastUsedAt(now)
                                .build())
                );
    }

    private static String normalizeDeviceId(String deviceId) {
        return deviceId.trim();
    }
}
