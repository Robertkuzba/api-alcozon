package com.alcoholfactory.api.modules.order.util;

import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import com.alcoholfactory.api.modules.order.domain.OrderDeliveryDetails;
import com.alcoholfactory.api.modules.user.domain.User;
import java.util.Map;

/**
 * Adres dostawy dla zamówienia własnego — z {@code preferences.delivery} lub pól płaskich w JSON.
 */
public final class CustomOrderDeliveryDetailsResolver {

  private CustomOrderDeliveryDetailsResolver() {}

  @SuppressWarnings("unchecked")
  public static OrderDeliveryDetails fromPreferences(CustomOrder order) {
    Map<String, Object> preferences = order.getPreferences();
    if (preferences != null) {
      Object nested = preferences.get("delivery");
      if (nested instanceof Map<?, ?> deliveryMap) {
        OrderDeliveryDetails built = fromMap((Map<String, Object>) deliveryMap);
        if (built.hasStructuredData()) {
          return built;
        }
      }
      OrderDeliveryDetails flat = fromMap(preferences);
      if (flat.hasStructuredData()) {
        return flat;
      }
    }
    User customer = order.getCustomer();
    String name =
        customer != null
            ? (nullable(customer.getFirstName()) + " " + nullable(customer.getLastName())).trim()
            : "";
    if (name.isBlank() && customer != null) {
      name = customer.getEmail();
    }
    return OrderDeliveryDetails.builder()
        .recipientName(name.isBlank() ? "Klient zamówienia własnego" : name)
        .streetAddress("Adres w konfiguratorze / preferences")
        .city("—")
        .postalCode("00-000")
        .country("Polska")
        .deliveryNotes(order.getDescription())
        .paymentMethod(null)
        .build();
  }

  private static OrderDeliveryDetails fromMap(Map<String, Object> map) {
    return OrderDeliveryDetails.builder()
        .recipientName(string(map.get("recipientName")))
        .streetAddress(string(map.get("streetAddress")))
        .city(string(map.get("city")))
        .postalCode(string(map.get("postalCode")))
        .country(stringOrDefault(map.get("country"), "Polska"))
        .deliveryNotes(string(map.get("deliveryNotes")))
        .paymentMethod(string(map.get("paymentMethod")))
        .build();
  }

  private static String string(Object raw) {
    if (raw == null) {
      return null;
    }
    String value = raw.toString().trim();
    return value.isEmpty() ? null : value;
  }

  private static String stringOrDefault(Object raw, String defaultValue) {
    String value = string(raw);
    return value != null ? value : defaultValue;
  }

  private static String nullable(String value) {
    return value == null ? "" : value;
  }
}
