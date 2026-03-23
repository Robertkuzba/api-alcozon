package com.alcoholfactory.api.common.validation;

public final class ValidationPatterns {

    private ValidationPatterns() {}

    /** Blokuje typowe znaki SQL/injection w polach tekstowych (imię, adres itd.). */
    public static final String SAFE_TEXT = "^[^;'\"<>\\{\\}\\[\\]\\\\]*$";

    public static final String PASSWORD_STRONG =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$";
}
