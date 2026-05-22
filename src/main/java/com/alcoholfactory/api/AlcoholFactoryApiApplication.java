package com.alcoholfactory.api;

import com.alcoholfactory.api.config.AppSecurityProperties;
import com.alcoholfactory.api.config.CorsProperties;
import com.alcoholfactory.api.config.DevNotificationTestHookProperties;
import com.alcoholfactory.api.config.FirebaseProperties;
import com.alcoholfactory.api.config.JwtProperties;
import com.alcoholfactory.api.config.MailProperties;
import com.alcoholfactory.api.config.PasswordResetProperties;
import com.alcoholfactory.api.config.TwoFactorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        AppSecurityProperties.class,
        CorsProperties.class,
        FirebaseProperties.class,
        DevNotificationTestHookProperties.class,
        MailProperties.class,
        PasswordResetProperties.class,
        TwoFactorProperties.class
})
public class AlcoholFactoryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlcoholFactoryApiApplication.class, args);
    }

}
