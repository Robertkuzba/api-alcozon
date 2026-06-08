package com.alcoholfactory.api.support;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.inventory.domain.ProductStock;
import com.alcoholfactory.api.modules.inventory.repository.ProductStockRepository;
import com.alcoholfactory.api.modules.product.domain.Product;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDataSeeder implements CommandLineRunner {

  public static final String MANAGER_EMAIL = "manager@example.com";
  public static final String MANAGER_PASSWORD = "Manager123!";
  public static final String EMPLOYEE_EMAIL = "employee@example.com";
  public static final String EMPLOYEE_PASSWORD = "Employee123!";
  public static final String CUSTOMER_EMAIL = "customer@example.com";
  public static final String CUSTOMER_PASSWORD = "Customer123!";

  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final ProductStockRepository productStockRepository;
  private final PasswordEncoder passwordEncoder;

  private static long productId;

  public static long seededProductId() {
    return productId;
  }

  @Override
  public void run(String... args) {
    seedUser(MANAGER_EMAIL, MANAGER_PASSWORD, UserRole.MANAGER, false, null);
    seedUser(EMPLOYEE_EMAIL, EMPLOYEE_PASSWORD, UserRole.EMPLOYEE, true, null);
    seedUser(CUSTOMER_EMAIL, CUSTOMER_PASSWORD, UserRole.CUSTOMER, false, Instant.now());

    productId =
        productStockRepository.findAll().stream()
            .filter(s -> s.getQuantity() > 0)
            .findFirst()
            .map(ProductStock::getId)
            .orElseGet(this::createFallbackProduct);
  }

  private long createFallbackProduct() {
    Product p =
        productRepository.save(
            Product.builder()
                .name("Test Vodka 500ml")
                .description("Integration test product")
                .category("vodka")
                .price(new BigDecimal("29.99"))
                .volumeMl(500)
                .abv(new BigDecimal("40.0"))
                .active(true)
                .build());
    productStockRepository.save(
        ProductStock.builder().product(p).quantity(100).warehouseZone("T1").build());
    return p.getId();
  }

  private void seedUser(
      String email, String password, UserRole role, boolean courier, Instant ageConfirmedAt) {
    if (userRepository.existsByEmail(email)) {
      return;
    }
    userRepository.save(
        User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .role(role)
            .active(true)
            .courier(courier)
            .ageConfirmedAt(ageConfirmedAt)
            .build());
  }
}
