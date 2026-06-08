package com.alcoholfactory.api.notification;

public final class OrderRealtimeDestinations {

  /** Klient (Web) — subskrypcja: {@code /user/queue/order-updates} */
  public static final String CUSTOMER_QUEUE = "/queue/order-updates";

  /** Magazyn + staff — subskrypcja: {@code /topic/orders/staff} */
  public static final String STAFF_TOPIC = "/topic/orders/staff";

  /** Desktop (manager) — przypisanie kuriera: {@code /topic/orders/dispatch} */
  public static final String DISPATCH_TOPIC = "/topic/orders/dispatch";

  /** Kurier — subskrypcja: {@code /user/queue/courier-deliveries} */
  public static final String COURIER_QUEUE = "/queue/courier-deliveries";

  private OrderRealtimeDestinations() {}
}
