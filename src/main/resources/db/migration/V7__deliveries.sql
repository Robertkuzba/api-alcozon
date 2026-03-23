CREATE TABLE deliveries (
    id               BIGSERIAL PRIMARY KEY,
    order_id         BIGINT NOT NULL UNIQUE REFERENCES orders (id) ON DELETE CASCADE,
    courier_id       BIGINT REFERENCES users (id),
    status           VARCHAR(32) NOT NULL
        CHECK (status IN ('PENDING', 'ASSIGNED', 'IN_TRANSIT', 'DELIVERED', 'FAILED')),
    address_snapshot TEXT NOT NULL,
    started_at       TIMESTAMPTZ,
    delivered_at     TIMESTAMPTZ
);

CREATE INDEX idx_deliveries_courier ON deliveries (courier_id);
CREATE INDEX idx_deliveries_status ON deliveries (status);
