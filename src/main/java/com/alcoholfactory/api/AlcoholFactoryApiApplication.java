package com.alcoholfactory.api;

import com.alcoholfactory.api.config.CorsProperties;
import com.alcoholfactory.api.config.FirebaseProperties;
import com.alcoholfactory.api.config.JwtProperties;
import com.alcoholfactory.api.config.MailProperties;
import com.alcoholfactory.api.config.TwoFactorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        CorsProperties.class,
        FirebaseProperties.class,
        MailProperties.class,
        TwoFactorProperties.class
})
public class AlcoholFactoryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlcoholFactoryApiApplication.class, args);
    }

}
