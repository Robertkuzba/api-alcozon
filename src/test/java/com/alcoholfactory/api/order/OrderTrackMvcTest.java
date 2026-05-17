package com.alcoholfactory.api.order;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.modules.order.util.OrderNumbers;
import com.alcoholfactory.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderTrackMvcTest extends AbstractIntegrationTest {

  private static final String CUSTOMER_EMAIL = "track-customer@example.com";

  @Autowired
  UserRepository userRepository;

  @Autowired
  CustomerOrderRepository orderRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  long orderId;

  @BeforeEach
  void seedOrder() {
    User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseGet(() -> userRepository.save(
        User.builder()
            .email(CUSTOMER_EMAIL)
            .passwordHash(passwordEncoder.encode("Customer123!"))
            .role(UserRole.CUSTOMER)
            .active(true)
            .courier(false)
            .build()
    ));

    CustomerOrder order = orderRepository.save(
        CustomerOrder.builder()
            .customer(customer)
            .status(OrderStatus.IN_PRODUCTION)
            .deliveryAddress("ul. Testowa 1, Warszawa")
            .totalAmount(new BigDecimal("99.99"))
            .build()
    );
    orderId = order.getId();
    order.setOrderNumber(OrderNumbers.format(orderId));
    orderRepository.save(order);
  }

  @Test
  void trackPublic_returnsOrderWhenEmailMatches() throws Exception {
    mockMvc.perform(get("/api/orders/track")
            .param("orderId", Long.toString(orderId))
            .param("email", CUSTOMER_EMAIL))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId))
        .andExpect(jsonPath("$.orderNumber").value("ORD-" + orderId))
        .andExpect(jsonPath("$.status").value("IN_PRODUCTION"));
  }

  @Test
  void trackPublic_returns404WhenEmailDoesNotMatch() throws Exception {
    mockMvc.perform(get("/api/orders/track")
            .param("orderId", Long.toString(orderId))
            .param("email", "wrong@example.com"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Order not found"));
  }

  @Test
  void trackPublic_returns404WhenOrderMissing() throws Exception {
    mockMvc.perform(get("/api/orders/track")
            .param("orderId", "999999")
            .param("email", CUSTOMER_EMAIL))
        .andExpect(status().isNotFound());
  }
}
