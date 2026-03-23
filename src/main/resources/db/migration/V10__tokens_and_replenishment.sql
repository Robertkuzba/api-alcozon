CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE replenishment_orders (
    id          BIGSERIAL PRIMARY KEY,
    manager_id  BIGINT NOT NULL REFERENCES users (id),
    note        TEXT,
    status      VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE replenishment_lines (
    id                 BIGSERIAL PRIMARY KEY,
    replenishment_id   BIGINT NOT NULL REFERENCES replenishment_orders (id) ON DELETE CASCADE,
    product_id         BIGINT REFERENCES products (id),
    raw_material_id    BIGINT REFERENCES raw_materials (id),
    quantity_delta     NUMERIC(14, 3) NOT NULL,
    CHECK (
        (product_id IS NOT NULL AND raw_material_id IS NULL)
        OR (product_id IS NULL AND raw_material_id IS NOT NULL)
    )
);

CREATE INDEX idx_replenishment_lines_order ON replenishment_lines (replenishment_id);
