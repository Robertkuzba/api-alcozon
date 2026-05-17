-- Strukturalne dane dostawy (zamiast jednego pola TEXT z etykietami)
ALTER TABLE orders
    ADD COLUMN recipient_name VARCHAR(200),
    ADD COLUMN street_address VARCHAR(500),
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN postal_code VARCHAR(20),
    ADD COLUMN country VARCHAR(100),
    ADD COLUMN delivery_notes TEXT,
    ADD COLUMN payment_method VARCHAR(100);

ALTER TABLE deliveries
    ADD COLUMN recipient_name VARCHAR(200),
    ADD COLUMN street_address VARCHAR(500),
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN postal_code VARCHAR(20),
    ADD COLUMN country VARCHAR(100),
    ADD COLUMN delivery_notes TEXT,
    ADD COLUMN payment_method VARCHAR(100);
