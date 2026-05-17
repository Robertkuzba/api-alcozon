package com.alcoholfactory.api.order.util;

import com.alcoholfactory.api.modules.order.util.OrderNumbers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderNumbersTest {

    @Test
    void format_and_parse() {
        assertThat(OrderNumbers.format(430721L)).isEqualTo("ORD-430721");
        assertThat(OrderNumbers.parseId("ORD-430721")).isEqualTo(430721L);
        assertThat(OrderNumbers.parseId("430721")).isEqualTo(430721L);
    }
}
