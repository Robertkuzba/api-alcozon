package com.alcoholfactory.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alcoholfactory.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AppCheckMvcTest extends AbstractIntegrationTest {

  private static final String DEBUG_SHA =
      "e1b17830399a952b8ff905023d5dc98f0a202cbb18941beb06000717341ac7f6";

  private static final String VALID_BODY =
      """
      {
        "platform": "android",
        "packageName": "com.alkozon.app",
        "versionName": "1.0.0",
        "versionCode": 1,
        "signingCertSha256": "%s"
      }
      """
          .formatted(DEBUG_SHA);

  @Test
  void appCheck_validDebugCert_returns204() throws Exception {
    mockMvc
        .perform(
            post("/api/security/app-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isNoContent());
  }

  @Test
  void appCheck_acceptsShaWithColons() throws Exception {
    String shaWithColons =
        "E1:B1:78:30:39:9A:95:2B:8F:F9:05:02:35:D5:DC:98:F0:A2:02:CB:"
            + "B1:89:41:BE:B0:60:00:71:73:41:AC:7F:6";
    String body =
        """
        {
          "platform": "android",
          "packageName": "com.alkozon.app",
          "versionName": "1.0.0",
          "versionCode": 1,
          "signingCertSha256": "%s"
        }
        """
            .formatted(shaWithColons);
    mockMvc
        .perform(
            post("/api/security/app-check").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isNoContent());
  }

  @Test
  void appCheck_wrongSha_returns403() throws Exception {
    String body = VALID_BODY.replace(DEBUG_SHA, "deadbeef".repeat(8));
    mockMvc
        .perform(
            post("/api/security/app-check").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  void appCheck_wrongPackage_returns403() throws Exception {
    String body = VALID_BODY.replace("com.alkozon.app", "com.evil.app");
    mockMvc
        .perform(
            post("/api/security/app-check").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  void appCheck_missingField_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/security/app-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"platform\":\"android\"}"))
        .andExpect(status().isBadRequest());
  }
}
