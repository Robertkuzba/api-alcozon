CREATE TABLE raw_materials (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    unit             VARCHAR(32)  NOT NULL,
    quantity         NUMERIC(14, 3) NOT NULL DEFAULT 0,
    last_updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE product_stock (
    product_id       BIGINT PRIMARY KEY REFERENCES products (id) ON DELETE CASCADE,
    quantity         INTEGER NOT NULL DEFAULT 0,
    warehouse_zone   VARCHAR(64),
    last_updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
