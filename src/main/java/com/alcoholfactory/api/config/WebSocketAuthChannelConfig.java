package com.alcoholfactory.api.config;

import com.alcoholfactory.api.notification.OrderRealtimeDestinations;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Set;
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
                if (accessor == null) {
                    return message;
                }
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    authenticateConnect(message, accessor);
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    authorizeSubscribe(message, accessor);
                }
                return message;
            }
        });
    }

    private void authenticateConnect(Message<?> message, StompHeaderAccessor accessor) {
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
        } catch (MessageDeliveryException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageDeliveryException(message, new BadCredentialsException("Invalid or expired STOMP token", e));
        }
    }

    private void authorizeSubscribe(Message<?> message, StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication auth)) {
            throw new MessageDeliveryException(message, new BadCredentialsException("Unauthenticated STOMP subscription"));
        }
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean customer = roles.contains("ROLE_CUSTOMER") || roles.contains("ROLE_GUEST");
        boolean staff = roles.contains("ROLE_EMPLOYEE") || roles.contains("ROLE_MANAGER");
        boolean manager = roles.contains("ROLE_MANAGER");

        if (destination.contains(OrderRealtimeDestinations.CUSTOMER_QUEUE)) {
            if (!customer) {
                deny(message, "Subscription not allowed for role");
            }
            return;
        }
        if (destination.contains(OrderRealtimeDestinations.STAFF_TOPIC)) {
            if (!staff) {
                deny(message, "Staff topic requires EMPLOYEE or MANAGER");
            }
            return;
        }
        if (destination.contains(OrderRealtimeDestinations.DISPATCH_TOPIC)) {
            if (!manager) {
                deny(message, "Dispatch topic requires MANAGER");
            }
            return;
        }
        if (destination.contains(OrderRealtimeDestinations.COURIER_QUEUE)) {
            if (!staff) {
                deny(message, "Courier queue requires EMPLOYEE or MANAGER");
            }
        }
    }

    private static void deny(Message<?> message, String reason) {
        throw new MessageDeliveryException(message, new AccessDeniedException(reason));
    }
}
