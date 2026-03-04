package com.alcoholfactory.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI alcoholFactoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Alcohol Factory API")
                        .description("API dla systemu firmy produkującej alkohol – Web, Mobile, Desktop")
                        .version("0.0.1-SNAPSHOT"))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local")
                ));
    }
}
