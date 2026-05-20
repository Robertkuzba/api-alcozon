package com.alcoholfactory.api.config;

import com.alcoholfactory.api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean devNotificationHook = environment.getProperty(
                "app.dev.notification-test-hook.enabled",
                Boolean.class,
                false
        );
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/docs/**",
                            "/swagger-ui/**",
                            "/api-docs/**",
                            "/v3/api-docs/**"
                    ).permitAll();
                    auth.requestMatchers(HttpMethod.POST,
                            "/api/auth/register",
                            "/api/auth/login",
                            "/api/auth/staff/login",
                            "/api/auth/staff/verify-device",
                            "/api/auth/refresh",
                            "/api/auth/guest"
                    ).permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/security/app-check").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/orders/track").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/custom-orders/track").permitAll();
                    auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    auth.requestMatchers("/ws/**").permitAll();
                    if (devNotificationHook) {
                        auth.requestMatchers(HttpMethod.POST, "/api/dev/notification-test/**").permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
