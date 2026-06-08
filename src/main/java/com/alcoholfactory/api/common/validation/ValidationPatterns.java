package com.alcoholfactory.api.common.validation;

public final class ValidationPatterns {

  private ValidationPatterns() {}

  /** Blokuje typowe znaki SQL/injection w polach tekstowych (imię, adres itd.). */
  public static final String SAFE_TEXT = "^[^;'\"<>\\{\\}\\[\\]\\\\]*$";

  public static final String PASSWORD_STRONG =
      "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$";

  /** Kod pocztowy PL: 00-000 lub 00000 */
  public static final String POSTAL_CODE_PL = "^[0-9]{2}-?[0-9]{3}$";

  /** Numer zamówienia z klienta (Web): cyfry, litery, myślnik */
  public static final String CLIENT_ORDER_NUMBER = "^[A-Za-z0-9-]{1,50}$";
}
