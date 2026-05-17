-- Numer zamówienia z frontu (Web) — osobno od systemowego order_number (ORD-{id})
ALTER TABLE orders
    ADD COLUMN client_order_number VARCHAR(50);

CREATE UNIQUE INDEX uk_orders_client_order_number
    ON orders (client_order_number)
    WHERE client_order_number IS NOT NULL;

ALTER TABLE deliveries
    ADD COLUMN client_order_number VARCHAR(50);
