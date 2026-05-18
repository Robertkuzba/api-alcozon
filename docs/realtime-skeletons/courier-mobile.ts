/**
 * Mobilka kurier — po staff login (ten sam JWT co magazyn).
 * Subskrypcja: /user/queue/courier-deliveries
 *
 * DELIVERY_ASSIGNED → odśwież GET /api/deliveries/my lub /api/orders/for-courier/{userId}
 * ORDER_DELIVERED → usuń z listy / potwierdź UI
 */
import { STOMP_DESTINATIONS } from "./types";
import { subscribeOrderRealtime } from "./stompSubscribe";
import type { OrderRealtimeEvent } from "./types";
import type { StompUnsubscribe } from "./stompSubscribe";

export function subscribeCourierDeliveries(
  accessToken: string,
  apiBaseUrl: string,
  onEvent: (event: OrderRealtimeEvent) => void
): StompUnsubscribe {
  return subscribeOrderRealtime(accessToken, apiBaseUrl, STOMP_DESTINATIONS.courier, onEvent);
}

export function handleCourierEvent(
  event: OrderRealtimeEvent,
  reloadDeliveries: () => void
): void {
  switch (event.type) {
    case "DELIVERY_ASSIGNED":
    case "ORDER_DELIVERED":
      reloadDeliveries();
      break;
    default:
      break;
  }
}
