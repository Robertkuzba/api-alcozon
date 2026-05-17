package com.alcoholfactory.api.auth;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.TestDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerRegistrationMvcTest extends AbstractIntegrationTest {

    @Test
    void register_withAgeConfirmed_setsAgeConfirmedAt_andAllowsOrder() throws Exception {
        String email = "kuba-reg-" + System.nanoTime() + "@test.pl";
        String registerBody = """
                {
                  "email": "%s",
                  "password": "SilneHaslo123!",
                  "firstName": "Jan",
                  "lastName": "Kowalski",
                  "ageConfirmed": true
                }
                """.formatted(email);

        MvcResult reg = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.ageConfirmedAt").isNotEmpty())
                .andReturn();

        String access = com.jayway.jsonpath.JsonPath.read(
                reg.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(post("/api/auth/confirm-age")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.ageConfirmedAt").isNotEmpty());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.ageConfirmedAt").isNotEmpty());

        long productId = TestDataSeeder.seededProductId();
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deliveryAddress": "ul. Testowa 1, 00-001 Warszawa",
                                  "items": [{"productId": %d, "quantity": 1}]
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_withoutAgeConfirmed_rejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "no-age-%d@test.pl",
                                  "password": "SilneHaslo123!",
                                  "firstName": "Jan",
                                  "lastName": "Kowalski",
                                  "ageConfirmed": false
                                }
                                """.formatted(System.nanoTime())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_asGuest_forbidden() throws Exception {
        MvcResult guest = mockMvc.perform(post("/api/auth/guest"))
                .andExpect(status().isOk())
                .andReturn();
        String guestToken = com.jayway.jsonpath.JsonPath.read(
                guest.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deliveryAddress": "ul. Testowa 1",
                                  "items": [{"productId": 1, "quantity": 1}]
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
