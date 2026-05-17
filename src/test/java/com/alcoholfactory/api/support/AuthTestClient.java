package com.alcoholfactory.api.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class AuthTestClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuthTestClient() {}

    public record Tokens(String accessToken, String refreshToken) {}

    public static Tokens login(MockMvc mockMvc, String email, String password) throws Exception {
        if (TestDataSeeder.EMPLOYEE_EMAIL.equals(email) || TestDataSeeder.MANAGER_EMAIL.equals(email)) {
            return staffLogin(mockMvc, email, password, "integration-test-device");
        }
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = MAPPER.readTree(result.getResponse().getContentAsString());
        return new Tokens(json.get("accessToken").asText(), json.get("refreshToken").asText());
    }

    public static Tokens staffLogin(MockMvc mockMvc, String email, String password, String deviceId) throws Exception {
        String body = """
                {"email":"%s","password":"%s","deviceId":"%s"}
                """.formatted(email, password, deviceId);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/staff/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = MAPPER.readTree(loginResult.getResponse().getContentAsString());
        if (loginJson.path("verificationRequired").asBoolean(false)) {
            String challengeId = loginJson.get("challengeId").asText();
            MvcResult verifyResult = mockMvc.perform(post("/api/auth/staff/verify-device")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"challengeId":"%s","deviceId":"%s","code":"1234"}
                                    """.formatted(challengeId, deviceId)))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode tokens = MAPPER.readTree(verifyResult.getResponse().getContentAsString());
            return new Tokens(tokens.get("accessToken").asText(), tokens.get("refreshToken").asText());
        }
        JsonNode tokens = loginJson.get("tokens");
        return new Tokens(tokens.get("accessToken").asText(), tokens.get("refreshToken").asText());
    }

    public static Tokens refresh(MockMvc mockMvc, String refreshToken) throws Exception {
        String body = """
                {"refreshToken":"%s"}
                """.formatted(refreshToken);

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = MAPPER.readTree(result.getResponse().getContentAsString());
        return new Tokens(json.get("accessToken").asText(), json.get("refreshToken").asText());
    }
}
