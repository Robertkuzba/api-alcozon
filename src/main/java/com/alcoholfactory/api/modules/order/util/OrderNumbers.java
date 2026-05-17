package com.alcoholfactory.api.modules.order.util;

public final class OrderNumbers {

    public static final String PREFIX = "ORD-";

    private OrderNumbers() {}

    public static String format(long orderId) {
        return PREFIX + orderId;
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
