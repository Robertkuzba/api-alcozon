package com.alcoholfactory.api.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import com.alcoholfactory.api.modules.order.repository.CustomOrderRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class CustomOrderTrackMvcTest extends AbstractIntegrationTest {

  private static final String CUSTOMER_EMAIL = "custom-track@example.com";
  private static final String CLIENT_NUMBER = "881122";

  @Autowired UserRepository userRepository;

  @Autowired CustomOrderRepository customOrderRepository;

  @Autowired PasswordEncoder passwordEncoder;

  long customOrderId;

  @BeforeEach
  void seedCustomOrder() {
    User customer =
        userRepository
            .findByEmail(CUSTOMER_EMAIL)
            .orElseGet(
                () ->
                    userRepository.save(
                        User.builder()
                            .email(CUSTOMER_EMAIL)
                            .passwordHash(passwordEncoder.encode("Customer123!"))
                            .role(UserRole.CUSTOMER)
                            .active(true)
                            .courier(false)
                            .ageConfirmedAt(java.time.Instant.now())
                            .build()));

    CustomOrder order =
        customOrderRepository
            .findByClientOrderNumberWithCustomer(CLIENT_NUMBER)
            .orElseGet(
                () ->
                    customOrderRepository.save(
                        CustomOrder.builder()
                            .customer(customer)
                            .description("Własna nalewka testowa")
                            .clientOrderNumber(CLIENT_NUMBER)
                            .status(OrderStatus.SUBMITTED)
                            .build()));
    customOrderId = order.getId();
  }

  @Test
  void trackPublic_byClientOrderNumber() throws Exception {
    mockMvc
        .perform(
            get("/api/custom-orders/track")
                .param("orderId", CLIENT_NUMBER)
                .param("email", CUSTOMER_EMAIL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customOrderId").value(customOrderId))
        .andExpect(jsonPath("$.clientOrderNumber").value(CLIENT_NUMBER))
        .andExpect(jsonPath("$.status").value("SUBMITTED"));
  }

  @Test
  void trackPublic_byCustomPrefixId() throws Exception {
    mockMvc
        .perform(
            get("/api/custom-orders/track")
                .param("orderId", "CUSTOM-" + customOrderId)
                .param("email", CUSTOMER_EMAIL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customOrderId").value(customOrderId));
  }

  @Test
  void trackPublic_returns404WhenEmailMismatch() throws Exception {
    mockMvc
        .perform(
            get("/api/custom-orders/track")
                .param("orderId", CLIENT_NUMBER)
                .param("email", "wrong@example.com"))
        .andExpect(status().isNotFound());
  }
}
