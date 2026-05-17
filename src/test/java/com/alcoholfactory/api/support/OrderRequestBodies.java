package com.alcoholfactory.api.support;

public final class OrderRequestBodies {

    private OrderRequestBodies() {}

    public static String createWithDelivery(long productId, String recipientName) {
        return """
                {
                  "items": [{"productId": %d, "quantity": 1}],
                  "delivery": {
                    "recipientName": "%s",
                    "streetAddress": "ul. Testowa 10",
                    "city": "Wrocław",
                    "postalCode": "50-001",
                    "country": "Polska",
                    "deliveryNotes": "domofon 12",
                    "paymentMethod": "Płatność przy odbiorze"
                  }
                }
                """.formatted(productId, recipientName);
    }
}
