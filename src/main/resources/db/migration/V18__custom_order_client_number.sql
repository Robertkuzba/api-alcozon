ALTER TABLE custom_orders
    ADD COLUMN client_order_number VARCHAR(50);

CREATE UNIQUE INDEX uk_custom_orders_client_order_number
    ON custom_orders (client_order_number)
    WHERE client_order_number IS NOT NULL;

-- Backfill z preferences (front wstawiał clientOrderNumber w JSON)
UPDATE custom_orders
SET client_order_number = preferences ->> 'clientOrderNumber'
WHERE client_order_number IS NULL
  AND preferences ->> 'clientOrderNumber' IS NOT NULL
  AND preferences ->> 'clientOrderNumber' <> '';
