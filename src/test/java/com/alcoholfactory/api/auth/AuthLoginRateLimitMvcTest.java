package com.alcoholfactory.api.auth;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthLoginRateLimitMvcTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void enableRateLimit(DynamicPropertyRegistry registry) {
        registry.add("app.rate-limit.enabled", () -> "true");
    }

  @Test
  void login_returns429AfterTooManyFailedAttemptsFromSameIp() throws Exception {
    String body = """
        {"email":"nobody@example.com","password":"wrong-password"}
        """;

    for (int i = 0; i < 10; i++) {
      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isUnauthorized());
    }

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isTooManyRequests());
  }
}
