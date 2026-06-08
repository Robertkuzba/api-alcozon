package com.alcoholfactory.api.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.AuthTestClient;
import com.alcoholfactory.api.support.TestDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class OrderStatusTransitionMvcTest extends AbstractIntegrationTest {

  @Test
  void patchStatus_rejectsInvalidTransition() throws Exception {
    AuthTestClient.Tokens employee =
        AuthTestClient.login(
            mockMvc, TestDataSeeder.EMPLOYEE_EMAIL, TestDataSeeder.EMPLOYEE_PASSWORD);

    mockMvc
        .perform(
            patch("/api/orders/999999/status")
                .header("Authorization", "Bearer " + employee.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DELIVERED\"}"))
        .andExpect(status().isNotFound());
  }
}
