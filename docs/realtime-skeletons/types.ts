/**
 * Wspólne typy payloadu STOMP — kopiuj do Web / Desktop lub importuj z monorepo.
 * Backend: OrderRealtimeEvent.java
 */

export type OrderRealtimeEventType =
  | "ORDER_SUBMITTED"
  | "ORDER_STATUS_CHANGED"
  | "DISPATCH_PENDING"
  | "DELIVERY_ASSIGNED"
  | "ORDER_DELIVERED"
  | "ORDER_CANCELLED";

export type BackendOrderStatus =
  | "SUBMITTED"
  | "IN_PRODUCTION"
  | "IN_PACKING"
  | "IN_DELIVERY"
  | "DELIVERED"
  | "CANCELLED";

export interface OrderRealtimeEvent {
  type: OrderRealtimeEventType;
  orderId: number;
  clientOrderNumber: string;
  status: BackendOrderStatus;
  deliveryId?: number;
  courierUserId?: number;
}

/** Destynacje (muszą zgadzać się z OrderRealtimeDestinations.java). */
export const STOMP_DESTINATIONS = {
  customer: "/user/queue/order-updates",
  staff: "/topic/orders/staff",
  dispatch: "/topic/orders/dispatch",
  courier: "/user/queue/courier-deliveries",
} as const;

export function resolveWsUrl(apiBaseUrl: string): string {
  const trimmed = apiBaseUrl.replace(/\/api\/?$/, "");
  if (trimmed.startsWith("https://")) {
    return trimmed.replace("https://", "wss://") + "/ws";
  }
  if (trimmed.startsWith("http://")) {
    return trimmed.replace("http://", "ws://") + "/ws";
  }
  return "ws://localhost:8080/ws";
}

export function parseOrderRealtimeEvent(body: string): OrderRealtimeEvent | null {
  try {
    const data = JSON.parse(body) as Partial<OrderRealtimeEvent>;
    if (typeof data.orderId !== "number" || typeof data.status !== "string") {
      return null;
    }
    return {
      type: data.type as OrderRealtimeEventType,
      orderId: data.orderId,
      clientOrderNumber: data.clientOrderNumber ?? String(data.orderId),
      status: data.status as BackendOrderStatus,
      deliveryId: data.deliveryId,
      courierUserId: data.courierUserId,
    };
  } catch {
    return null;
  }
}
