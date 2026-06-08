package com.alcoholfactory.api.modules.order.dto;

import com.alcoholfactory.api.modules.order.domain.OrderDeliveryDetails;

public record OrderDeliveryDetailsResponse(
    String recipientName,
    String streetAddress,
    String city,
    String postalCode,
    String country,
    String deliveryNotes,
    String paymentMethod) {
  public static OrderDeliveryDetailsResponse from(OrderDeliveryDetails details) {
    if (details == null || !details.hasStructuredData()) {
      return null;
    }
    return new OrderDeliveryDetailsResponse(
        details.getRecipientName(),
        details.getStreetAddress(),
        details.getCity(),
        details.getPostalCode(),
        details.getCountry(),
        details.getDeliveryNotes(),
        details.getPaymentMethod());
  }
}
