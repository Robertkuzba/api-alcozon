package com.alcoholfactory.api.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class HealthEndpointMvcTest extends AbstractIntegrationTest {

  @Test
  void health_isPublicAndUp() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }
}
