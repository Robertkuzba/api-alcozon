CREATE TABLE products (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    category     VARCHAR(100),
    price        NUMERIC(12, 2) NOT NULL,
    volume_ml    INTEGER,
    abv          NUMERIC(5, 2),
    image_url    VARCHAR(500),
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_active ON products (is_active);
