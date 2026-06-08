package com.alcoholfactory.api.notification;

import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;

public interface OrderRealtimeNotifier {

  void onOrderCreated(CustomerOrder order);

  void onOrderStatusChanged(CustomerOrder order, OrderStatus newStatus);

  void onOrderCancelled(CustomerOrder order);

  void onDeliveryAssigned(Delivery delivery);

  void onOrderDelivered(CustomerOrder order, Delivery delivery);
}
