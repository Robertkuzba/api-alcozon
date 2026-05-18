-- Usunięcie zduplikowanych pól: adres w kolumnach strukturalnych, numer = client_order_number
UPDATE orders
SET client_order_number = 'LEGACY-' || id
WHERE client_order_number IS NULL;

ALTER TABLE orders
    ALTER COLUMN client_order_number SET NOT NULL;

ALTER TABLE orders
    DROP COLUMN IF EXISTS delivery_address;

ALTER TABLE orders
    DROP COLUMN IF EXISTS order_number;

ALTER TABLE deliveries
    DROP COLUMN IF EXISTS address_snapshot;

ALTER TABLE deliveries
    DROP COLUMN IF EXISTS order_number;
