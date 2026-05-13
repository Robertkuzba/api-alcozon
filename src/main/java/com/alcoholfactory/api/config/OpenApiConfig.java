package com.alcoholfactory.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearer-jwt";

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${app.openapi.server-url:}")
    private String publicServerUrl;

    @Bean
    public OpenAPI alcoholFactoryOpenAPI() {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url("http://localhost:" + serverPort).description("Local"));
        if (StringUtils.hasText(publicServerUrl)) {
            servers.add(new Server().url(publicServerUrl.trim()).description("Production (Render)"));
        }
        return new OpenAPI()
                .info(new Info()
                        .title("Alcohol Factory API")
                        .description("API dla systemu firmy produkującej alkohol – Web, Mobile, Desktop")
                        .version("0.0.1-SNAPSHOT"))
                .servers(servers)
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
