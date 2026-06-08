package com.alcoholfactory.api.security;

import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties properties;
  private final SecretKey secretKey;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Long userId, String email, UserRole role) {
    return buildToken(userId, email, role, properties.accessTtl() * 1000);
  }

  private String buildToken(Long userId, String email, UserRole role, long ttlMillis) {
    Instant now = Instant.now();
    Date issued = Date.from(now);
    Date exp = Date.from(now.plusMillis(ttlMillis));
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("email", email)
        .claim("role", role.name())
        .issuedAt(issued)
        .expiration(exp)
        .signWith(secretKey)
        .compact();
  }

  public Claims parseAccessToken(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  public String generateRefreshTokenValue() {
    byte[] bytes = new byte[32];
    new java.security.SecureRandom().nextBytes(bytes);
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public long refreshTtlSeconds() {
    return properties.refreshTtl();
  }

  public long accessTtlSeconds() {
    return properties.accessTtl();
  }
}
