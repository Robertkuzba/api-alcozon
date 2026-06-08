package com.alcoholfactory.api.modules.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDeliveryDetails {

  @Column(name = "recipient_name", length = 200)
  private String recipientName;

  @Column(name = "street_address", length = 500)
  private String streetAddress;

  @Column(name = "city", length = 100)
  private String city;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(name = "country", length = 100)
  private String country;

  @Column(name = "delivery_notes", columnDefinition = "TEXT")
  private String deliveryNotes;

  @Column(name = "payment_method", length = 100)
  private String paymentMethod;

  public boolean hasStructuredData() {
    return recipientName != null
        && !recipientName.isBlank()
        && streetAddress != null
        && !streetAddress.isBlank()
        && city != null
        && !city.isBlank()
        && postalCode != null
        && !postalCode.isBlank();
  }

  public static OrderDeliveryDetails copyOf(OrderDeliveryDetails source) {
    if (source == null) {
      return null;
    }
    return OrderDeliveryDetails.builder()
        .recipientName(source.getRecipientName())
        .streetAddress(source.getStreetAddress())
        .city(source.getCity())
        .postalCode(source.getPostalCode())
        .country(source.getCountry())
        .deliveryNotes(source.getDeliveryNotes())
        .paymentMethod(source.getPaymentMethod())
        .build();
  }
}
