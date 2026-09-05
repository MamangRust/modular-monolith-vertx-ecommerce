-- Commerce tables used by the cart/order/order-item/shipping services.
-- All lifecycle tables use deleted_at for soft deletion; active uniqueness is
-- enforced with partial indexes so a trashed record can be recreated safely.

CREATE TABLE IF NOT EXISTS categories (
    category_id   SERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    slug_category VARCHAR(255) NOT NULL,
    image_category TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS products (
    product_id      SERIAL PRIMARY KEY,
    merchant_id     INT NOT NULL REFERENCES merchants (merchant_id),
    category_id     INT NOT NULL REFERENCES categories (category_id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price           INT NOT NULL CHECK (price >= 0),
    count_in_stock  INT NOT NULL DEFAULT 0 CHECK (count_in_stock >= 0),
    brand           VARCHAR(255),
    weight          INT NOT NULL DEFAULT 0 CHECK (weight >= 0),
    rating          REAL NOT NULL DEFAULT 0,
    slug_product    VARCHAR(255) NOT NULL,
    image_product   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS carts (
    cart_id     SERIAL PRIMARY KEY,
    user_id     INT NOT NULL REFERENCES users (user_id),
    product_id  INT NOT NULL REFERENCES products (product_id),
    name        VARCHAR(255) NOT NULL,
    price       INT NOT NULL CHECK (price >= 0),
    image       TEXT,
    quantity    INT NOT NULL CHECK (quantity > 0),
    weight      INT NOT NULL DEFAULT 0 CHECK (weight >= 0),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    order_id    SERIAL PRIMARY KEY,
    merchant_id INT NOT NULL REFERENCES merchants (merchant_id),
    user_id     INT NOT NULL REFERENCES users (user_id),
    total_price INT NOT NULL DEFAULT 0 CHECK (total_price >= 0),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id      INT NOT NULL REFERENCES orders (order_id),
    product_id    INT NOT NULL REFERENCES products (product_id),
    quantity      INT NOT NULL CHECK (quantity > 0),
    price         INT NOT NULL CHECK (price >= 0),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS shipping_addresses (
    shipping_address_id SERIAL PRIMARY KEY,
    order_id            INT NOT NULL REFERENCES orders (order_id),
    alamat              TEXT NOT NULL,
    provinsi            VARCHAR(255) NOT NULL,
    negara              VARCHAR(255) NOT NULL,
    kota                VARCHAR(255) NOT NULL,
    courier             VARCHAR(100) NOT NULL,
    shipping_method     VARCHAR(100) NOT NULL,
    shipping_cost       INT NOT NULL DEFAULT 0 CHECK (shipping_cost >= 0),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_active_slug
    ON categories (slug_category) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_products_active_slug
    ON products (slug_product) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_shipping_active_order
    ON shipping_addresses (order_id) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_products_active_merchant
    ON products (merchant_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_products_active_category
    ON products (category_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_carts_active_user
    ON carts (user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_order_items_active_order
    ON order_items (order_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_orders_active_user
    ON orders (user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_orders_active_merchant
    ON orders (merchant_id) WHERE deleted_at IS NULL;

-- V9 predates the Java transaction contract. Keep its legacy columns for
-- compatibility, but make them optional/defaulted and add the fields used by
-- the current transaction service.
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS order_id INT REFERENCES orders (order_id),
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);

ALTER TABLE transactions
    ALTER COLUMN card_number DROP NOT NULL,
    ALTER COLUMN transaction_time SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN transaction_time DROP NOT NULL,
    ALTER COLUMN payment_status SET DEFAULT 'pending';

UPDATE transactions
SET payment_status = COALESCE(payment_status, status, 'pending')
WHERE payment_status IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_transactions_active_idempotency
    ON transactions (idempotency_key)
    WHERE deleted_at IS NULL AND idempotency_key IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_transactions_active_order
    ON transactions (order_id) WHERE deleted_at IS NULL;
