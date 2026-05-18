/**
 * Mobilka magazyn — EMPLOYEE / MANAGER.
 * Subskrypcja: /topic/orders/staff
 *
 * Reaguj m.in. na:
 * - ORDER_SUBMITTED → odśwież GET /api/orders (lista)
 * - ORDER_STATUS_CHANGED → zaktualizuj wiersz / usuń przy IN_DELIVERY
 */
import { STOMP_DESTINATIONS } from "./types";
import { subscribeOrderRealtime } from "./stompSubscribe";
import type { OrderRealtimeEvent } from "./types";
import type { StompUnsubscribe } from "./stompSubscribe";

export function subscribeStaffWarehouseUpdates(
  accessToken: string,
  apiBaseUrl: string,
  onEvent: (event: OrderRealtimeEvent) => void
): StompUnsubscribe {
  return subscribeOrderRealtime(accessToken, apiBaseUrl, STOMP_DESTINATIONS.staff, onEvent);
}

/** Przykład handlera listy zamówień magazynu */
export function handleStaffWarehouseEvent(
  event: OrderRealtimeEvent,
  reloadOrders: () => void
): void {
  switch (event.type) {
    case "ORDER_SUBMITTED":
    case "ORDER_STATUS_CHANGED":
    case "ORDER_CANCELLED":
    case "ORDER_DELIVERED":
      reloadOrders();
      break;
    default:
      break;
  }
}
