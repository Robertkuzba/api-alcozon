package com.alcoholfactory.api.order.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.alcoholfactory.api.modules.order.util.OrderNumbers;
import org.junit.jupiter.api.Test;

class OrderNumbersTest {

  @Test
  void format_and_parse() {
    assertThat(OrderNumbers.format(430721L)).isEqualTo("ORD-430721");
    assertThat(OrderNumbers.parseId("ORD-430721")).isEqualTo(430721L);
    assertThat(OrderNumbers.parseId("430721")).isEqualTo(430721L);
    assertThat(OrderNumbers.parseId("246077")).isEqualTo(246077L);
  }

  @Test
  void parseId_returnsNullForNonNumericClientNumber() {
    assertThat(OrderNumbers.parseId("ALK-2026-0001")).isNull();
  }
}
