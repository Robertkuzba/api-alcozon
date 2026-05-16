package com.alcoholfactory.api.auth;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.AuthTestClient;
import com.alcoholfactory.api.support.TestDataSeeder;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRefreshMvcTest extends AbstractIntegrationTest {

    @Test
    void refresh_issuesNewAccessTokenAndMeEndpointWorks() throws Exception {
        AuthTestClient.Tokens first = AuthTestClient.login(mockMvc, TestDataSeeder.MANAGER_EMAIL, TestDataSeeder.MANAGER_PASSWORD);

        AuthTestClient.Tokens refreshed = AuthTestClient.refresh(mockMvc, first.refreshToken());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + refreshed.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TestDataSeeder.MANAGER_EMAIL))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void refresh_rejectsAlreadyUsedRefreshToken() throws Exception {
        AuthTestClient.Tokens first = AuthTestClient.login(mockMvc, TestDataSeeder.EMPLOYEE_EMAIL, TestDataSeeder.EMPLOYEE_PASSWORD);
        AuthTestClient.refresh(mockMvc, first.refreshToken());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + first.refreshToken() + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
