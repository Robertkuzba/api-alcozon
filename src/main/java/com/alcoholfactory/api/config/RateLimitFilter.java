package com.alcoholfactory.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Login: 10 prób / 15 min / IP. Wyszukiwarka produktów (param q): 5 req/s / IP.
 */
@Component
@Order(0)
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> searchBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String ip = clientIp(request);

        if ("POST".equalsIgnoreCase(request.getMethod()) && path.contains("/auth/login")) {
            Bucket bucket = loginBuckets.computeIfAbsent(ip, k -> loginBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", "60");
                response.getWriter().write("Too many login attempts");
                return;
            }
        }

        if ("GET".equalsIgnoreCase(request.getMethod())
                && path.contains("/products")
                && request.getParameter("q") != null
                && !request.getParameter("q").isBlank()) {
            Bucket bucket = searchBuckets.computeIfAbsent(ip, k -> searchBucket());
            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", "1");
                response.getWriter().write("Product search rate limit exceeded");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static Bucket loginBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(15)));
        return Bucket.builder().addLimit(limit).build();
    }

    private static Bucket searchBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
