package com.alcoholfactory.api.modules.auth.service;

import com.alcoholfactory.api.config.TwoFactorProperties;
import com.alcoholfactory.api.security.TokenHasher;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final TwoFactorProperties twoFactorProperties;

  public String generatePlainCode() {
    if (twoFactorProperties.useFixedCode()) {
      return twoFactorProperties.fixedCodeForTests().trim();
    }
    int value = RANDOM.nextInt(10_000);
    return String.format("%04d", value);
  }

  public String hashCode(String plainCode) {
    return TokenHasher.sha256Hex(plainCode.trim());
  }

  public boolean matches(String plainCode, String storedHash) {
    return hashCode(plainCode).equals(storedHash);
  }
}
