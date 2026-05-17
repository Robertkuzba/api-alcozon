package com.alcoholfactory.api.modules.order.util;

import com.alcoholfactory.api.modules.order.domain.OrderDeliveryDetails;

public final class DeliveryAddressFormatter {

    private DeliveryAddressFormatter() {}

    /** Wieloliniowy opis (pole legacy {@code delivery_address} / {@code address_snapshot}). */
    public static String formatMultiline(OrderDeliveryDetails d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Imię i nazwisko: ").append(d.getRecipientName().trim()).append('\n');
        sb.append("Adres: ").append(d.getStreetAddress().trim()).append('\n');
        sb.append("Miasto: ").append(d.getCity().trim()).append('\n');
        sb.append("Kod pocztowy: ").append(d.getPostalCode().trim()).append('\n');
        String country = d.getCountry();
        if (country != null && !country.isBlank()) {
            sb.append("Kraj: ").append(country.trim()).append('\n');
        }
        if (d.getDeliveryNotes() != null && !d.getDeliveryNotes().isBlank()) {
            sb.append("Uwagi dla dostawcy: ").append(d.getDeliveryNotes().trim()).append('\n');
        }
        if (d.getPaymentMethod() != null && !d.getPaymentMethod().isBlank()) {
            sb.append("Metoda płatności: ").append(d.getPaymentMethod().trim());
        }
        return sb.toString().stripTrailing();
    }
}
