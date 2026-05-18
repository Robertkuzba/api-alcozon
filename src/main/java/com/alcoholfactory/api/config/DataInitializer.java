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

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User manager = User.builder()
                    .email("manager@example.com")
                    .passwordHash(passwordEncoder.encode("Manager123!"))
                    .role(UserRole.MANAGER)
                    .active(true)
                    .courier(false)
                    .build();
            userRepository.save(manager);
            User employee = User.builder()
                    .email("employee@example.com")
                    .passwordHash(passwordEncoder.encode("Employee123!"))
                    .role(UserRole.EMPLOYEE)
                    .active(true)
                    .courier(true)
                    .build();
            userRepository.save(employee);
            log.info("Seeded users: manager@example.com / Manager123!, employee@example.com / Employee123!");
        }
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
}
