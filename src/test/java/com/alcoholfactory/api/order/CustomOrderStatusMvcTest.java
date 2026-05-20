package com.alcoholfactory.api.order;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import com.alcoholfactory.api.modules.order.repository.CustomOrderRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.support.AbstractIntegrationTest;
import com.alcoholfactory.api.support.AuthTestClient;
import com.alcoholfactory.api.support.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomOrderStatusMvcTest extends AbstractIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CustomOrderRepository customOrderRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    long customOrderId;
    String employeeToken;

    @BeforeEach
    void seed() throws Exception {
        User customer = userRepository.findByEmail("custom-status@example.com").orElseGet(() -> userRepository.save(
                User.builder()
                        .email("custom-status@example.com")
                        .passwordHash(passwordEncoder.encode("Customer123!"))
                        .role(UserRole.CUSTOMER)
                        .active(true)
                        .courier(false)
                        .ageConfirmedAt(java.time.Instant.now())
                        .build()
        ));

        CustomOrder order = customOrderRepository.save(
                CustomOrder.builder()
                        .customer(customer)
                        .description("Test custom unified status")
                        .clientOrderNumber("990011")
                        .status(OrderStatus.SUBMITTED)
                        .build()
        );
        customOrderId = order.getId();
        employeeToken = AuthTestClient.login(
                mockMvc,
                TestDataSeeder.EMPLOYEE_EMAIL,
                TestDataSeeder.EMPLOYEE_PASSWORD
        ).accessToken();
    }

    @Test
    void patchStatus_toInDelivery_createsDelivery() throws Exception {
        for (String status : new String[] {"IN_PRODUCTION", "IN_PACKING", "IN_DELIVERY"}) {
            mockMvc.perform(patch("/api/custom-orders/" + customOrderId + "/status")
                            .header("Authorization", "Bearer " + employeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + status + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(status));
        }

        assertThat(deliveryRepository.findByCustomOrderId(customOrderId)).isPresent();
    }
}
