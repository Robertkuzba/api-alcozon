/**
 * Desktop (manager) — przypisanie kuriera.
 * Subskrypcje: /topic/orders/staff + /topic/orders/dispatch (tylko MANAGER)
 *
 * DISPATCH_PENDING → pokaż toast / podświetl zamówienie IN_DELIVERY (deliveryId w payloadzie)
 */
import { STOMP_DESTINATIONS } from "./types";
import { subscribeOrderRealtime } from "./stompSubscribe";
import type { OrderRealtimeEvent } from "./types";
import type { StompUnsubscribe } from "./stompSubscribe";

export function subscribeDispatchBoard(
  accessToken: string,
  apiBaseUrl: string,
  handlers: {
    onStaffEvent?: (event: OrderRealtimeEvent) => void;
    onDispatchPending?: (event: OrderRealtimeEvent) => void;
  }
): StompUnsubscribe {
  const unsubs: StompUnsubscribe[] = [];

  unsubs.push(
    subscribeOrderRealtime(accessToken, apiBaseUrl, STOMP_DESTINATIONS.staff, (event) => {
      handlers.onStaffEvent?.(event);
    })
  );

  unsubs.push(
    subscribeOrderRealtime(accessToken, apiBaseUrl, STOMP_DESTINATIONS.dispatch, (event) => {
      if (event.type === "DISPATCH_PENDING") {
        handlers.onDispatchPending?.(event);
      }
    })
  );

  return () => {
    unsubs.forEach((u) => u());
  };
}

/** Po evencie: GET /api/deliveries lub lista zamówień IN_DELIVERY */
export function handleDispatchPending(
  event: OrderRealtimeEvent,
  openAssignDialog: (orderId: number, deliveryId: number | undefined) => void
): void {
  openAssignDialog(event.orderId, event.deliveryId);
}
