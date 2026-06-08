package com.alcoholfactory.api.modules.order.util;

public final class OrderNumbers {

  public static final String PREFIX = "ORD-";

  private OrderNumbers() {}

  public static String format(long orderId) {
    return PREFIX + orderId;
  }

  /** Parsuje ORD-123 lub samo 123 do id zamówienia (kompatybilność wsteczna w track/get). */
  public static Long parseId(String orderRef) {
    if (orderRef == null || orderRef.isBlank()) {
      return null;
    }
    String raw = orderRef.trim();
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
