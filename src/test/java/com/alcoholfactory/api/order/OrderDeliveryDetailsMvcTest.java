package com.alcoholfactory.api.order;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.AuthTestClient;
import com.alcoholfactory.api.support.OrderRequestBodies;
import com.alcoholfactory.api.support.TestDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class OrderDeliveryDetailsMvcTest extends AbstractIntegrationTest {

  @Test
  void createOrder_persistsStructuredDeliveryDetails() throws Exception {
    AuthTestClient.Tokens customer =
        AuthTestClient.login(
            mockMvc, TestDataSeeder.CUSTOMER_EMAIL, TestDataSeeder.CUSTOMER_PASSWORD);

    mockMvc
        .perform(
            post("/api/orders")
                .header("Authorization", "Bearer " + customer.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    OrderRequestBodies.createWithDelivery(
                        TestDataSeeder.seededProductId(), "Jakub Janiec", "430721")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.clientOrderNumber").value("430721"))
        .andExpect(jsonPath("$.deliveryDetails.recipientName").value("Jakub Janiec"))
        .andExpect(jsonPath("$.deliveryDetails.city").value("Wrocław"))
        .andExpect(jsonPath("$.deliveryDetails.postalCode").value("50-001"))
        .andExpect(jsonPath("$.deliveryDetails.paymentMethod").value("Płatność przy odbiorze"));
  }

  @Test
  void getOrder_returnsOrderIdAsNumericId_notEmbeddedInAddress() throws Exception {
    AuthTestClient.Tokens customer =
        AuthTestClient.login(
            mockMvc, TestDataSeeder.CUSTOMER_EMAIL, TestDataSeeder.CUSTOMER_PASSWORD);

    var created =
        mockMvc
            .perform(
                post("/api/orders")
                    .header("Authorization", "Bearer " + customer.accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        OrderRequestBodies.createWithDelivery(
                            TestDataSeeder.seededProductId(), "Jan Kowalski", "430721")))
            .andExpect(status().isCreated())
            .andReturn();

    long orderId =
        com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");

    mockMvc
        .perform(
            get("/api/orders/" + orderId)
                .header("Authorization", "Bearer " + customer.accessToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId))
        .andExpect(jsonPath("$.clientOrderNumber").value("430721"))
        .andExpect(jsonPath("$.deliveryDetails.recipientName").value("Jan Kowalski"));
  }
}
