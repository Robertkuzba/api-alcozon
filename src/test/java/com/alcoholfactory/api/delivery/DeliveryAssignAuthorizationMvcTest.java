package com.alcoholfactory.api.delivery;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.AuthTestClient;
import com.alcoholfactory.api.support.OrderRequestBodies;
import com.alcoholfactory.api.support.TestDataSeeder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeliveryAssignAuthorizationMvcTest extends AbstractIntegrationTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void assign_forbiddenForEmployee_allowedForManager() throws Exception {
        AuthTestClient.Tokens customer = AuthTestClient.login(
                mockMvc, TestDataSeeder.CUSTOMER_EMAIL, TestDataSeeder.CUSTOMER_PASSWORD);
        AuthTestClient.Tokens employee = AuthTestClient.login(
                mockMvc, TestDataSeeder.EMPLOYEE_EMAIL, TestDataSeeder.EMPLOYEE_PASSWORD);
        AuthTestClient.Tokens manager = AuthTestClient.login(
                mockMvc, TestDataSeeder.MANAGER_EMAIL, TestDataSeeder.MANAGER_PASSWORD);

        long productId = TestDataSeeder.seededProductId();
        MvcResult created = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customer.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OrderRequestBodies.createWithDelivery(productId, "Klient Testowy")))
                .andExpect(status().isCreated())
                .andReturn();

        long orderId = JSON.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        for (String status : new String[] {"IN_PRODUCTION", "IN_PACKING", "IN_DELIVERY"}) {
            mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                            .header("Authorization", "Bearer " + employee.accessToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + status + "\"}"))
                    .andExpect(status().isOk());
        }

        long courierId = JSON.readTree(mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/me")
                        .header("Authorization", "Bearer " + employee.accessToken()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).get("id").asLong();

        MvcResult deliveries = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/deliveries")
                        .header("Authorization", "Bearer " + manager.accessToken()))
                .andExpect(status().isOk())
                .andReturn();

        long deliveryId = 0;
        for (JsonNode d : JSON.readTree(deliveries.getResponse().getContentAsString())) {
            if (d.get("orderId").asLong() == orderId) {
                deliveryId = d.get("id").asLong();
                break;
            }
        }

        mockMvc.perform(patch("/api/deliveries/" + deliveryId + "/assign")
                        .header("Authorization", "Bearer " + employee.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courierId\":" + courierId + "}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/deliveries/" + deliveryId + "/assign")
                        .header("Authorization", "Bearer " + manager.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courierId\":" + courierId + "}"))
                .andExpect(status().isOk());
    }
}
