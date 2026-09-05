-- Auth service expects verification_code & is_verified on users, plus reset_tokens table.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS verification_code VARCHAR(255) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS reset_tokens (
    reset_token_id SERIAL PRIMARY KEY,
    user_id        INT NOT NULL REFERENCES users (user_id),
    token          VARCHAR(255) NOT NULL UNIQUE,
    expiry_date    TIMESTAMP NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reset_tokens_user_id ON reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_reset_tokens_token ON reset_tokens (token);
CREATE INDEX IF NOT EXISTS idx_reset_tokens_expiry_date ON reset_tokens (expiry_date);
