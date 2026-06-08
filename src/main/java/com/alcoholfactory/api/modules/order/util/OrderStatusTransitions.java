package com.alcoholfactory.api.modules.order.util;

import com.alcoholfactory.api.common.domain.OrderStatus;

public final class OrderStatusTransitions {

  private OrderStatusTransitions() {}

  public static boolean isAllowed(OrderStatus from, OrderStatus to) {
    if (from == to) {
      return true;
    }
    if (to == OrderStatus.CANCELLED) {
      return from == OrderStatus.SUBMITTED
          || from == OrderStatus.IN_PRODUCTION
          || from == OrderStatus.IN_PACKING;
    }
    return switch (from) {
      case SUBMITTED -> to == OrderStatus.IN_PRODUCTION || to == OrderStatus.IN_PACKING;
      case IN_PRODUCTION -> to == OrderStatus.IN_PACKING || to == OrderStatus.IN_DELIVERY;
      case IN_PACKING -> to == OrderStatus.IN_DELIVERY;
      case IN_DELIVERY -> to == OrderStatus.DELIVERED;
      default -> false;
    };
  }
}
