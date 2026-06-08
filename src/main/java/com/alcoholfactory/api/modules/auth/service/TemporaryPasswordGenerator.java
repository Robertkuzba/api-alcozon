package com.alcoholfactory.api.modules.auth.service;

import java.security.SecureRandom;

/** Losowe hasło tymczasowe (min. 8 znaków) do resetu hasła pracownika. */
public final class TemporaryPasswordGenerator {

  private static final int DEFAULT_LENGTH = 12;
  private static final int MIN_LENGTH = 8;
  private static final String CHARS =
      "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%&*";

  private static final SecureRandom RANDOM = new SecureRandom();

  private TemporaryPasswordGenerator() {}

  public static String generate() {
    return generate(DEFAULT_LENGTH);
  }

  public static String generate(int length) {
    int len = Math.max(length, MIN_LENGTH);
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
