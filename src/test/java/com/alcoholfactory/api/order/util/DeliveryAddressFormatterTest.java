package com.alcoholfactory.api.order.util;

import com.alcoholfactory.api.modules.order.domain.OrderDeliveryDetails;
import com.alcoholfactory.api.modules.order.util.DeliveryAddressFormatter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryAddressFormatterTest {

    @Test
    void formatMultiline_includesRecipientAndPayment() {
        OrderDeliveryDetails d = OrderDeliveryDetails.builder()
                .recipientName("Jakub Janiec")
                .streetAddress("Wrocławska")
                .city("Wrocław")
                .postalCode("54-540")
                .country("Polska")
                .deliveryNotes("domek jednorodzinny")
                .paymentMethod("Płatność przy odbiorze")
                .build();

        String text = DeliveryAddressFormatter.formatMultiline(d);

        assertThat(text).contains("Imię i nazwisko: Jakub Janiec");
        assertThat(text).contains("Kod pocztowy: 54-540");
        assertThat(text).doesNotContain("Numer zamówienia");
    }
}
