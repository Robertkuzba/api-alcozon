CREATE TABLE fcm_device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token TEXT NOT NULL,
    platform VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fcm_device_tokens_token UNIQUE (token)
);

CREATE INDEX idx_fcm_device_tokens_user_id ON fcm_device_tokens (user_id);
