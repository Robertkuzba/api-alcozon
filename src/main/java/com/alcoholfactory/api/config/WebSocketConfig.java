package com.alcoholfactory.api.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final AllowedOriginsProvider allowedOriginsProvider;

  public WebSocketConfig(AllowedOriginsProvider allowedOriginsProvider) {
    this.allowedOriginsProvider = allowedOriginsProvider;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    List<String> origins = allowedOriginsProvider.mergedAllowedOrigins();
    var registration = registry.addEndpoint("/ws");
    if (origins.isEmpty()) {
      registration.setAllowedOriginPatterns("*");
    } else {
      registration.setAllowedOrigins(origins.toArray(new String[0]));
    }
  }
}
