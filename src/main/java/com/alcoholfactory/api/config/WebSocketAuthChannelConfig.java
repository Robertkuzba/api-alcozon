package com.alcoholfactory.api.config;

import com.alcoholfactory.api.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthChannelConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    return message;
                }
                List<String> headers = accessor.getNativeHeader("Authorization");
                if (headers == null || headers.isEmpty()) {
                    throw new MessageDeliveryException(message, new BadCredentialsException("Missing Authorization for STOMP CONNECT"));
                }
                String raw = headers.getFirst();
                if (raw == null || !raw.startsWith("Bearer ")) {
                    throw new MessageDeliveryException(message, new BadCredentialsException("STOMP CONNECT requires Bearer token"));
                }
                String token = raw.substring(7).trim();
                if (token.isEmpty()) {
                    throw new MessageDeliveryException(message, new BadCredentialsException("Empty bearer token"));
                }
                try {
                    Claims claims = jwtService.parseAccessToken(token);
                    String email = claims.get("email", String.class);
                    String role = claims.get("role", String.class);
                    if (email == null) {
                        throw new MessageDeliveryException(message, new BadCredentialsException("Token without email claim"));
                    }
                    var authorities = role == null
                            ? List.<SimpleGrantedAuthority>of()
                            : Stream.of(role)
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toList());
                    var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    accessor.setUser(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    throw new MessageDeliveryException(message, new BadCredentialsException("Invalid or expired STOMP token", e));
                }
                return message;
            }
        });
    }
}
