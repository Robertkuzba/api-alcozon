package com.alcoholfactory.api.modules.order.util;

import java.util.UUID;

public final class OrderNumbers {

    public static final String PREFIX = "ORD-";

    /** Zgodne z {@code orders.order_number VARCHAR(32)}. */
    public static final int MAX_ORDER_NUMBER_LENGTH = 32;

    private OrderNumbers() {}

    public static String format(long orderId) {
        return PREFIX + orderId;
    }

    /**
     * Unikalny placeholder przed znanym {@code id} (kolumna max 32 znaki — pełny UUID się nie mieści).
     */
    public static String temporaryPlaceholder() {
        return "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 31);
    }

    /** Parsuje ORD-123 lub samo 123 do id zamówienia. */
    public static Long parseId(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return null;
        }
        String raw = orderNumber.trim();
        if (raw.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            raw = raw.substring(PREFIX.length());
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
