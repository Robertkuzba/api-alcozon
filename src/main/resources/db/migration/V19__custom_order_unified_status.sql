-- Ujednolicenie statusów custom_orders ze sklepem (OrderStatus) + dostawy dla zamówień własnych.

ALTER TABLE custom_orders DROP CONSTRAINT IF EXISTS custom_orders_status_check;

ALTER TABLE custom_orders
    ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMPTZ;

UPDATE custom_orders
SET status = CASE status
    WHEN 'PENDING' THEN 'SUBMITTED'
    WHEN 'IN_PROGRESS' THEN 'IN_PRODUCTION'
    WHEN 'COMPLETED' THEN 'DELIVERED'
    WHEN 'REJECTED' THEN 'CANCELLED'
    ELSE status
END;

ALTER TABLE custom_orders
    ADD CONSTRAINT custom_orders_status_check
        CHECK (status IN (
            'SUBMITTED',
            'IN_PRODUCTION',
            'IN_PACKING',
            'IN_DELIVERY',
            'DELIVERED',
            'CANCELLED'
        ));

ALTER TABLE deliveries
    ALTER COLUMN order_id DROP NOT NULL;

ALTER TABLE deliveries
    ADD COLUMN custom_order_id BIGINT UNIQUE REFERENCES custom_orders (id) ON DELETE CASCADE;

ALTER TABLE deliveries
    ADD CONSTRAINT deliveries_order_or_custom_chk
        CHECK (
            (order_id IS NOT NULL AND custom_order_id IS NULL)
            OR (order_id IS NULL AND custom_order_id IS NOT NULL)
        );

CREATE INDEX idx_deliveries_custom_order ON deliveries (custom_order_id);
