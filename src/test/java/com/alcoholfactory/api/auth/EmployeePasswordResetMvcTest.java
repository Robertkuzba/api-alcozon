package com.alcoholfactory.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.TestDataSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class EmployeePasswordResetMvcTest extends AbstractIntegrationTest {

  private static final String FIXED_RESET_PASSWORD = "ResetTest99!";

  @Autowired UserRepository userRepository;

  @Autowired PasswordEncoder passwordEncoder;

  @DynamicPropertySource
  static void passwordResetTestProps(DynamicPropertyRegistry registry) {
    registry.add("app.mail.log-only", () -> "true");
    registry.add("app.password-reset.fixed-password-for-tests", () -> FIXED_RESET_PASSWORD);
  }

  @AfterEach
  void restoreSeededStaffPasswords() {
    userRepository
        .findByEmail(TestDataSeeder.EMPLOYEE_EMAIL)
        .ifPresent(
            user -> {
              user.setPasswordHash(passwordEncoder.encode(TestDataSeeder.EMPLOYEE_PASSWORD));
              userRepository.save(user);
            });
    userRepository
        .findByEmail(TestDataSeeder.MANAGER_EMAIL)
        .ifPresent(
            user -> {
              user.setPasswordHash(passwordEncoder.encode(TestDataSeeder.MANAGER_PASSWORD));
              userRepository.save(user);
            });
  }

  @Test
  void requestReset_employee_updatesPassword_andReturns204() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s"}
                    """
                        .formatted(TestDataSeeder.EMPLOYEE_EMAIL)))
        .andExpect(status().isNoContent());

    var user = userRepository.findByEmail(TestDataSeeder.EMPLOYEE_EMAIL).orElseThrow();
    assertThat(user.getRole()).isEqualTo(UserRole.EMPLOYEE);
    assertThat(passwordEncoder.matches(FIXED_RESET_PASSWORD, user.getPasswordHash())).isTrue();
    assertThat(passwordEncoder.matches(TestDataSeeder.EMPLOYEE_PASSWORD, user.getPasswordHash()))
        .isFalse();
  }

  @Test
  void requestReset_customer_doesNotChangePassword_andReturns204() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s"}
                    """
                        .formatted(TestDataSeeder.CUSTOMER_EMAIL)))
        .andExpect(status().isNoContent());

    var user = userRepository.findByEmail(TestDataSeeder.CUSTOMER_EMAIL).orElseThrow();
    assertThat(passwordEncoder.matches(TestDataSeeder.CUSTOMER_PASSWORD, user.getPasswordHash()))
        .isTrue();
  }

  @Test
  void requestReset_manager_updatesPassword_andReturns204() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s"}
                    """
                        .formatted(TestDataSeeder.MANAGER_EMAIL)))
        .andExpect(status().isNoContent());

    var user = userRepository.findByEmail(TestDataSeeder.MANAGER_EMAIL).orElseThrow();
    assertThat(user.getRole()).isEqualTo(UserRole.MANAGER);
    assertThat(passwordEncoder.matches(FIXED_RESET_PASSWORD, user.getPasswordHash())).isTrue();
    assertThat(passwordEncoder.matches(TestDataSeeder.MANAGER_PASSWORD, user.getPasswordHash()))
        .isFalse();
  }

  @Test
  void requestReset_unknownEmail_returns204() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"nobody-here@example.com"}
                    """))
        .andExpect(status().isNoContent());
  }

  @Test
  void requestReset_noAuthorizationRequired() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s"}
                    """
                        .formatted(TestDataSeeder.EMPLOYEE_EMAIL)))
        .andExpect(status().isNoContent());
  }
}
