package com.alcoholfactory.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.TestDataSeeder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class StaffAuthMvcTest extends AbstractIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String DEVICE_ID = "test-device-integration-01";

  @DynamicPropertySource
  static void enableTwoFactor(DynamicPropertyRegistry registry) {
    registry.add("app.two-factor.enabled", () -> "true");
    registry.add("app.two-factor.fixed-code-for-tests", () -> "1234");
    registry.add("app.mail.log-only", () -> "true");
  }

  @Test
  void staffLogin_requiresVerification_thenVerifyIssuesTokens() throws Exception {
    String loginBody =
        """
        {
          "email":"%s",
          "password":"%s",
          "deviceId":"%s"
        }
        """
            .formatted(TestDataSeeder.EMPLOYEE_EMAIL, TestDataSeeder.EMPLOYEE_PASSWORD, DEVICE_ID);

    String pendingJson =
        mockMvc
            .perform(
                post("/api/auth/staff/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verificationRequired").value(true))
            .andExpect(jsonPath("$.challengeId").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode pending = JSON.readTree(pendingJson);
    String challengeId = pending.get("challengeId").asText();

    mockMvc
        .perform(
            post("/api/auth/staff/verify-device")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "challengeId":"%s",
                      "deviceId":"%s",
                      "code":"1234"
                    }
                    """
                        .formatted(challengeId, DEVICE_ID)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.role").value("EMPLOYEE"));
  }

  @Test
  void staffLogin_trustedDevice_skipsVerificationOnSecondLogin() throws Exception {
    String deviceId = "trusted-device-02";
    String loginBody =
        """
        {
          "email":"%s",
          "password":"%s",
          "deviceId":"%s"
        }
        """
            .formatted(TestDataSeeder.MANAGER_EMAIL, TestDataSeeder.MANAGER_PASSWORD, deviceId);

    String first =
        mockMvc
            .perform(
                post("/api/auth/staff/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verificationRequired").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String challengeId = JSON.readTree(first).get("challengeId").asText();
    mockMvc
        .perform(
            post("/api/auth/staff/verify-device")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"challengeId":"%s","deviceId":"%s","code":"1234"}
                    """
                        .formatted(challengeId, deviceId)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/auth/staff/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.verificationRequired").value(false))
        .andExpect(jsonPath("$.tokens.accessToken").exists());
  }

  @Test
  void regularLogin_rejectsStaff() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s","password":"%s"}
                    """
                        .formatted(
                            TestDataSeeder.EMPLOYEE_EMAIL, TestDataSeeder.EMPLOYEE_PASSWORD)))
        .andExpect(status().isBadRequest());
  }
}
