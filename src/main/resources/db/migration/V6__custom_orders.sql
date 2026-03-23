CREATE TABLE custom_orders (
    id            BIGSERIAL PRIMARY KEY,
    customer_id   BIGINT NOT NULL REFERENCES users (id),
    description   TEXT NOT NULL,
    preferences   JSONB,
    status        VARCHAR(32) NOT NULL
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED')),
    assigned_to   BIGINT REFERENCES users (id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_custom_orders_customer ON custom_orders (customer_id);
CREATE INDEX idx_custom_orders_status ON custom_orders (status);
