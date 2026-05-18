/**
 * Web — klient (CUSTOMER). Subskrypcja: /user/queue/order-updates
 *
 * Użycie (React):
 *   useEffect(() => {
 *     if (!token) return;
 *     return subscribeCustomerOrderUpdates(token, API_URL, (ev) => {
 *       if (ev.type === "ORDER_DELIVERED") refetchOrder(ev.orderId);
 *     });
 *   }, [token]);
 */
import { STOMP_DESTINATIONS } from "./types";
import { subscribeOrderRealtime } from "./stompSubscribe";
import type { OrderRealtimeEvent } from "./types";
import type { StompUnsubscribe } from "./stompSubscribe";

export function subscribeCustomerOrderUpdates(
  accessToken: string,
  apiBaseUrl: string,
  onEvent: (event: OrderRealtimeEvent) => void
): StompUnsubscribe {
  return subscribeOrderRealtime(
    accessToken,
    apiBaseUrl,
    STOMP_DESTINATIONS.customer,
    onEvent
  );
}
