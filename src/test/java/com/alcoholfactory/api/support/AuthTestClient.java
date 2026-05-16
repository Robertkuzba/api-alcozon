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
