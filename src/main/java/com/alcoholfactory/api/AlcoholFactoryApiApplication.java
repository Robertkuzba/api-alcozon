package com.alcoholfactory.api;

import com.alcoholfactory.api.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class AlcoholFactoryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlcoholFactoryApiApplication.class, args);
    }

}
