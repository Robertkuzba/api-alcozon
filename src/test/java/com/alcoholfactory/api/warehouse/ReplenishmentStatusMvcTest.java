package com.alcoholfactory.api.warehouse;

import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.AuthTestClient;
import com.alcoholfactory.api.support.TestDataSeeder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReplenishmentStatusMvcTest extends AbstractIntegrationTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    ProductRepository productRepository;

    @Test
    void createStartsPending_patchReceivesAppliesStock() throws Exception {
        AuthTestClient.Tokens manager = AuthTestClient.login(
                mockMvc, TestDataSeeder.MANAGER_EMAIL, TestDataSeeder.MANAGER_PASSWORD);
        long productId = productRepository.findAll().stream()
                .findFirst()
                .orElseThrow()
                .getId();

        MvcResult created = mockMvc.perform(post("/api/warehouse/replenishment")
                        .header("Authorization", "Bearer " + manager.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"test order","lines":[{"productId":%d,"quantityDelta":3}]}
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        JsonNode body = JSON.readTree(created.getResponse().getContentAsString());
        long orderId = body.get("id").asLong();

        mockMvc.perform(patch("/api/warehouse/replenishment/" + orderId)
                        .header("Authorization", "Bearer " + manager.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RECEIVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        mockMvc.perform(patch("/api/warehouse/replenishment/" + orderId)
                        .header("Authorization", "Bearer " + manager.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isConflict());
    }
}
