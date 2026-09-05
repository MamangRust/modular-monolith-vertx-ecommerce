-- Commerce tables missing from V13. All lifecycle tables use deleted_at for
-- soft deletion; active uniqueness is enforced with partial indexes so a
-- trashed record can be recreated safely.

CREATE TABLE IF NOT EXISTS reviews (
    review_id   SERIAL PRIMARY KEY,
    user_id     INT NOT NULL REFERENCES users (user_id),
    product_id  INT NOT NULL REFERENCES products (product_id),
    name        VARCHAR(255) NOT NULL,
    comment     TEXT,
    rating      INT NOT NULL DEFAULT 5 CHECK (rating BETWEEN 1 AND 5),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS review_details (
    review_detail_id SERIAL PRIMARY KEY,
    review_id   INT NOT NULL REFERENCES reviews (review_id),
    type        VARCHAR(100) NOT NULL,
    url         TEXT NOT NULL,
    caption     VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS banners (
    banner_id   SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    start_date  DATE,
    end_date    DATE,
    start_time  TIME,
    end_time    TIME,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS sliders (
    slider_id   SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    image       TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS merchant_certifications_and_awards (
    merchant_certification_id SERIAL PRIMARY KEY,
    merchant_id     INT NOT NULL REFERENCES merchants (merchant_id),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    issued_by       VARCHAR(255),
    issue_date      DATE,
    expiry_date     DATE,
    certificate_url TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS merchant_business_information (
    merchant_business_info_id SERIAL PRIMARY KEY,
    merchant_id         INT NOT NULL REFERENCES merchants (merchant_id),
    business_type       VARCHAR(100),
    tax_id              VARCHAR(100),
    established_year    INT,
    number_of_employees INT,
    website_url         TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS merchant_details (
    merchant_detail_id SERIAL PRIMARY KEY,
    merchant_id         INT NOT NULL REFERENCES merchants (merchant_id),
    display_name        VARCHAR(255),
    cover_image_url     TEXT,
    logo_url            TEXT,
    short_description   TEXT,
    website_url         TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS merchant_policies (
    merchant_policy_id SERIAL PRIMARY KEY,
    merchant_id     INT NOT NULL REFERENCES merchants (merchant_id),
    policy_type     VARCHAR(100) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS merchant_social_media_links (
    merchant_social_id SERIAL PRIMARY KEY,
    merchant_detail_id INT NOT NULL REFERENCES merchant_details (merchant_detail_id),
    platform        VARCHAR(100) NOT NULL,
    url             TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP DEFAULT NULL
);

-- Active uniqueness / lookups (same pattern as V13)
CREATE UNIQUE INDEX IF NOT EXISTS uq_merchant_details_active_merchant
    ON merchant_details (merchant_id) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reviews_active_product
    ON reviews (product_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_reviews_active_user
    ON reviews (user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_review_details_active_review
    ON review_details (review_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_banners_active
    ON banners (is_active) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_sliders_active
    ON sliders (deleted_at);
CREATE INDEX IF NOT EXISTS idx_merchant_awards_active_merchant
    ON merchant_certifications_and_awards (merchant_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_merchant_business_active_merchant
    ON merchant_business_information (merchant_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_merchant_policies_active_merchant
    ON merchant_policies (merchant_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_merchant_social_active_detail
    ON merchant_social_media_links (merchant_detail_id) WHERE deleted_at IS NULL;
