package com.alcoholfactory.api.dev;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.TestDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.dev.notification-test-hook.enabled=true")
class NotificationTestHookMvcTest extends AbstractIntegrationTest {

  @Test
  void hook_createsOrderAndAssignsToEmployee_withoutAuth() throws Exception {
    mockMvc
        .perform(post("/api/dev/notification-test/order-assigned-to-employee"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderId").isNumber())
        .andExpect(jsonPath("$.deliveryId").isNumber())
        .andExpect(jsonPath("$.courierEmail").value(TestDataSeeder.EMPLOYEE_EMAIL));
  }
}
