/**
 * Bazowy helper STOMP (@stomp/stompjs) — używany przez szkielety klientów.
 *
 * npm install @stomp/stompjs
 */
import { Client, type IMessage } from "@stomp/stompjs";
import { parseOrderRealtimeEvent, resolveWsUrl, type OrderRealtimeEvent } from "./types";

export type StompUnsubscribe = () => void;

export function subscribeOrderRealtime(
  accessToken: string,
  apiBaseUrl: string,
  destination: string,
  onEvent: (event: OrderRealtimeEvent) => void
): StompUnsubscribe {
  const client = new Client({
    brokerURL: resolveWsUrl(apiBaseUrl),
    reconnectDelay: 5000,
    connectHeaders: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  const handleMessage = (message: IMessage) => {
    const event = parseOrderRealtimeEvent(message.body);
    if (event) {
      onEvent(event);
    }
  };

  client.onConnect = () => {
    client.subscribe(destination, handleMessage);
  };

  client.onStompError = (frame) => {
    console.warn("[STOMP]", destination, frame.headers["message"] ?? "broker error");
  };

  client.activate();

  return () => {
    void client.deactivate();
  };
}
