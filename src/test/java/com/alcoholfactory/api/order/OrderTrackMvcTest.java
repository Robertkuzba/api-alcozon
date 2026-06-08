package com.alcoholfactory.api.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import com.alcoholfactory.api.modules.order.domain.OrderDeliveryDetails;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class OrderTrackMvcTest extends AbstractIntegrationTest {

  private static final String CUSTOMER_EMAIL = "track-customer@example.com";
  private static final String CLIENT_ORDER_NUMBER = "246077";

  @Autowired UserRepository userRepository;

  @Autowired CustomerOrderRepository orderRepository;

  @Autowired PasswordEncoder passwordEncoder;

  long orderId;

  @BeforeEach
  void seedOrder() {
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
                            .build()));

    CustomerOrder order =
        orderRepository.save(
            CustomerOrder.builder()
                .customer(customer)
                .status(OrderStatus.IN_PRODUCTION)
                .clientOrderNumber(CLIENT_ORDER_NUMBER)
                .deliveryDetails(
                    OrderDeliveryDetails.builder()
                        .recipientName("Jan Test")
                        .streetAddress("ul. Testowa 1")
                        .city("Warszawa")
                        .postalCode("00-001")
                        .country("Polska")
                        .build())
                .totalAmount(new BigDecimal("99.99"))
                .build());
    orderId = order.getId();
  }

  @Test
  void trackPublic_returnsOrderWhenEmailMatchesByTechnicalId() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/track")
                .param("orderId", Long.toString(orderId))
                .param("email", CUSTOMER_EMAIL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId))
        .andExpect(jsonPath("$.clientOrderNumber").value(CLIENT_ORDER_NUMBER))
        .andExpect(jsonPath("$.status").value("IN_PRODUCTION"));
  }

  @Test
  void trackPublic_returnsOrderWhenEmailMatchesByClientOrderNumber() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/track")
                .param("orderId", CLIENT_ORDER_NUMBER)
                .param("email", CUSTOMER_EMAIL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId))
        .andExpect(jsonPath("$.clientOrderNumber").value(CLIENT_ORDER_NUMBER));
  }

  @Test
  void trackPublic_returns404WhenEmailDoesNotMatch() throws Exception {
    mockMvc
        .perform(
            get("/api/orders/track")
                .param("orderId", Long.toString(orderId))
                .param("email", "wrong@example.com"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Order not found"));
  }

  @Test
  void trackPublic_returns404WhenOrderMissing() throws Exception {
    mockMvc
        .perform(get("/api/orders/track").param("orderId", "999999").param("email", CUSTOMER_EMAIL))
        .andExpect(status().isNotFound());
  }
}
