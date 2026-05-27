package com.alcoholfactory.api.modules.auth.service;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.config.PasswordResetProperties;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeePasswordResetService {

    private static final Set<UserRole> STAFF_RESET_ROLES =
            EnumSet.of(UserRole.EMPLOYEE, UserRole.MANAGER);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeePasswordResetMailService mailService;
    private final PasswordResetProperties passwordResetProperties;

    /**
     * Reset hasła dla aktywnego użytkownika z rolą {@link UserRole#EMPLOYEE} lub {@link UserRole#MANAGER}.
     * Zawsze kończy się bez wyjątku (kontroler zwraca 204), także gdy e-mail nie istnieje
     * lub rola jest inna — bez ujawniania, czy konto jest w systemie.
     */
    @Transactional
    public void requestReset(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return;
        }
        userRepository.findByEmail(normalized).ifPresent(user -> {
            if (!STAFF_RESET_ROLES.contains(user.getRole()) || !user.isActive()) {
                return;
            }
            applyReset(user);
        });
    }

    private void applyReset(User user) {
        String plain = passwordResetProperties.useFixedPassword()
                ? passwordResetProperties.fixedPasswordForTests().trim()
                : TemporaryPasswordGenerator.generate();
        user.setPasswordHash(passwordEncoder.encode(plain));
        userRepository.save(user);
        mailService.sendNewPassword(user.getEmail(), plain, user.getRole());
    }
}
