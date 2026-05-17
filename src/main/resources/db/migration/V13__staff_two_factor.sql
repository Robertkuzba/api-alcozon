-- PostgreSQL (Flyway). Dialekt IDE: Languages & Frameworks → SQL Dialects → PostgreSQL.

CREATE TABLE trusted_devices (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    device_id    VARCHAR(128) NOT NULL,
    last_used_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_trusted_devices_user_device UNIQUE (user_id, device_id)
);

CREATE INDEX idx_trusted_devices_user_id ON trusted_devices (user_id);

CREATE TABLE device_verification_challenges (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    device_id   VARCHAR(128) NOT NULL,
    code_hash   VARCHAR(64)  NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_device_challenges_user_device ON device_verification_challenges (user_id, device_id);
CREATE INDEX idx_device_challenges_expires ON device_verification_challenges (expires_at);
