package com.alcoholfactory.api.config;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.inventory.domain.ProductStock;
import com.alcoholfactory.api.modules.inventory.repository.ProductStockRepository;
import com.alcoholfactory.api.modules.product.domain.Product;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String MANAGER_EMAIL = "manager@example.com";
    private static final String MANAGER_PASSWORD = "Manager123!";
    private static final String EMPLOYEE_EMAIL = "employee@example.com";
    private static final String EMPLOYEE_PASSWORD = "Employee123!";

    @Override
    public void run(String... args) {
        ensureDemoStaffUser(MANAGER_EMAIL, MANAGER_PASSWORD, UserRole.MANAGER, false);
        ensureDemoStaffUser(EMPLOYEE_EMAIL, EMPLOYEE_PASSWORD, UserRole.EMPLOYEE, true);
        if (productRepository.count() == 0) {
            Product p = Product.builder()
                    .name("Demo Vodka 500ml")
                    .description("Przykładowy produkt na start")
                    .category("vodka")
                    .price(new BigDecimal("49.99"))
                    .volumeMl(500)
                    .abv(new BigDecimal("40.0"))
                    .active(true)
                    .build();
            productRepository.save(p);
            productStockRepository.save(ProductStock.builder()
                    .product(p)
                    .quantity(100)
                    .warehouseZone("A1")
                    .build());
            log.info("Seeded demo product with stock");
        }
    }

    /**
     * Konta demo staff: tworzy przy braku, synchronizuje hasło/rolę jeśli ktoś zmienił hash w DB
     * (Neon produkcyjny — seed przy count()==0 nie nadpisuje istniejących użytkowników).
     */
    private void ensureDemoStaffUser(String email, String plainPassword, UserRole role, boolean courier) {
        userRepository.findByEmail(email).ifPresentOrElse(
                existing -> syncDemoStaffUser(existing, plainPassword, role, courier),
                () -> {
                    userRepository.save(User.builder()
                            .email(email)
                            .passwordHash(passwordEncoder.encode(plainPassword))
                            .role(role)
                            .active(true)
                            .courier(courier)
                            .build());
                    log.info("Seeded demo user: {}", email);
                }
        );
    }

    private void syncDemoStaffUser(User user, String plainPassword, UserRole role, boolean courier) {
        boolean changed = false;
        if (!passwordEncoder.matches(plainPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(plainPassword));
            changed = true;
        }
        if (user.getRole() != role) {
            user.setRole(role);
            changed = true;
        }
        if (!user.isActive()) {
            user.setActive(true);
            changed = true;
        }
        if (user.isCourier() != courier) {
            user.setCourier(courier);
            changed = true;
        }
        if (changed) {
            userRepository.save(user);
            log.info("Synchronized demo staff account: {}", user.getEmail());
        }
    }
}
