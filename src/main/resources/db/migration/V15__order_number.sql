-- Osobny numer zamówienia (np. ORD-430721) — do UI, raportów i dostaw
ALTER TABLE orders
    ADD COLUMN order_number VARCHAR(32);

UPDATE orders
SET order_number = 'ORD-' || id
WHERE order_number IS NULL;

ALTER TABLE orders
    ALTER COLUMN order_number SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT uk_orders_order_number UNIQUE (order_number);

ALTER TABLE deliveries
    ADD COLUMN order_number VARCHAR(32);

UPDATE deliveries d
SET order_number = o.order_number
FROM orders o
WHERE d.order_id = o.id
  AND d.order_number IS NULL;

UPDATE deliveries
SET order_number = 'ORD-' || order_id
WHERE order_number IS NULL;

ALTER TABLE deliveries
    ALTER COLUMN order_number SET NOT NULL;
