-- Travelify Module 2: Trip Management (templates, agent trips, services, add-ons)
-- Compatible with PostgreSQL and H2 (MODE=PostgreSQL)

-- ---------------------------------------------------------------------------
-- Extend users for provider roles (hotel / insurance / visa / travel agent)
-- ---------------------------------------------------------------------------
ALTER TABLE users ADD COLUMN provider_type VARCHAR(30);
ALTER TABLE users ADD COLUMN business_name VARCHAR(255);
ALTER TABLE users ADD COLUMN business_address TEXT;

ALTER TABLE users ADD CONSTRAINT ck_users_provider_type
    CHECK (provider_type IS NULL OR provider_type IN ('TRAVEL_AGENT', 'HOTEL', 'INSURANCE', 'VISA'));

CREATE INDEX idx_users_provider_type ON users (provider_type);

-- ---------------------------------------------------------------------------
-- trip_categories
-- ---------------------------------------------------------------------------
CREATE TABLE trip_categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    slug            VARCHAR(100) NOT NULL,
    description     TEXT,
    icon            VARCHAR(100),
    sort_order      INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_trip_categories_slug UNIQUE (slug)
);

CREATE INDEX idx_trip_categories_active ON trip_categories (is_active);

-- ---------------------------------------------------------------------------
-- trip_templates (Admin-created base trips)
-- ---------------------------------------------------------------------------
CREATE TABLE trip_templates (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL,
    short_description   TEXT,
    full_description    TEXT,
    featured_image      VARCHAR(500),
    category_id         BIGINT,
    difficulty          VARCHAR(50),
    duration_days       INT,
    min_age             INT,
    max_group_size      INT,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by          BIGINT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_trip_templates_slug UNIQUE (slug),
    CONSTRAINT ck_trip_templates_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT fk_trip_templates_category
        FOREIGN KEY (category_id) REFERENCES trip_categories (id),
    CONSTRAINT fk_trip_templates_created_by
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE INDEX idx_trip_templates_status ON trip_templates (status);
CREATE INDEX idx_trip_templates_category ON trip_templates (category_id);
CREATE INDEX idx_trip_templates_featured ON trip_templates (is_featured);
CREATE INDEX idx_trip_templates_created_by ON trip_templates (created_by);

-- ---------------------------------------------------------------------------
-- agent_trips (Agent-specific trips; may inherit from a template)
-- ---------------------------------------------------------------------------
CREATE TABLE agent_trips (
    id                  BIGSERIAL PRIMARY KEY,
    template_id         BIGINT,
    agent_id            BIGINT NOT NULL,
    title               VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL,
    short_description   TEXT,
    full_description    TEXT,
    featured_image      VARCHAR(500),
    category_id         BIGINT,
    difficulty          VARCHAR(50),
    duration_days       INT,
    min_age             INT,
    max_group_size      INT,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    override_fields     JSON,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_agent_trips_agent_slug UNIQUE (agent_id, slug),
    CONSTRAINT ck_agent_trips_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT fk_agent_trips_template
        FOREIGN KEY (template_id) REFERENCES trip_templates (id) ON DELETE SET NULL,
    CONSTRAINT fk_agent_trips_agent
        FOREIGN KEY (agent_id) REFERENCES users (id),
    CONSTRAINT fk_agent_trips_category
        FOREIGN KEY (category_id) REFERENCES trip_categories (id)
);

CREATE INDEX idx_agent_trips_agent ON agent_trips (agent_id);
CREATE INDEX idx_agent_trips_status ON agent_trips (status);
CREATE INDEX idx_agent_trips_agent_status ON agent_trips (agent_id, status);
CREATE INDEX idx_agent_trips_template ON agent_trips (template_id);
CREATE INDEX idx_agent_trips_category ON agent_trips (category_id);
CREATE INDEX idx_agent_trips_featured ON agent_trips (is_featured);

-- ---------------------------------------------------------------------------
-- agent_trip_itinerary
-- ---------------------------------------------------------------------------
CREATE TABLE agent_trip_itinerary (
    id              BIGSERIAL PRIMARY KEY,
    agent_trip_id   BIGINT NOT NULL,
    day_number      INT NOT NULL,
    title           VARCHAR(255),
    description     TEXT,
    activities      TEXT,
    accommodation   VARCHAR(255),
    meals           VARCHAR(255),
    CONSTRAINT fk_agent_trip_itinerary_trip
        FOREIGN KEY (agent_trip_id) REFERENCES agent_trips (id) ON DELETE CASCADE,
    CONSTRAINT uq_agent_trip_itinerary_day UNIQUE (agent_trip_id, day_number)
);

CREATE INDEX idx_agent_trip_itinerary_trip ON agent_trip_itinerary (agent_trip_id);

-- ---------------------------------------------------------------------------
-- agent_trip_pricing
-- ---------------------------------------------------------------------------
CREATE TABLE agent_trip_pricing (
    id                  BIGSERIAL PRIMARY KEY,
    agent_trip_id       BIGINT NOT NULL,
    pricing_type        VARCHAR(20) NOT NULL,
    price               DECIMAL(10, 2),
    currency            VARCHAR(3) NOT NULL DEFAULT 'USD',
    adult_price         DECIMAL(10, 2),
    child_price         DECIMAL(10, 2),
    infant_price        DECIMAL(10, 2),
    min_participants    INT,
    max_participants    INT,
    CONSTRAINT ck_agent_trip_pricing_type
        CHECK (pricing_type IN ('PER_PERSON', 'PER_GROUP', 'FIXED')),
    CONSTRAINT fk_agent_trip_pricing_trip
        FOREIGN KEY (agent_trip_id) REFERENCES agent_trips (id) ON DELETE CASCADE
);

CREATE INDEX idx_agent_trip_pricing_trip ON agent_trip_pricing (agent_trip_id);

-- ---------------------------------------------------------------------------
-- agent_trip_departures
-- ---------------------------------------------------------------------------
CREATE TABLE agent_trip_departures (
    id                  BIGSERIAL PRIMARY KEY,
    agent_trip_id       BIGINT NOT NULL,
    departure_date      DATE NOT NULL,
    end_date            DATE,
    available_seats     INT,
    price_override      DECIMAL(10, 2),
    is_cancelled        BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_agent_trip_departures_trip
        FOREIGN KEY (agent_trip_id) REFERENCES agent_trips (id) ON DELETE CASCADE
);

CREATE INDEX idx_agent_trip_departures_trip ON agent_trip_departures (agent_trip_id);
CREATE INDEX idx_agent_trip_departures_date ON agent_trip_departures (departure_date);
CREATE INDEX idx_agent_trip_departures_trip_date ON agent_trip_departures (agent_trip_id, departure_date);

-- ---------------------------------------------------------------------------
-- agent_trip_images
-- ---------------------------------------------------------------------------
CREATE TABLE agent_trip_images (
    id              BIGSERIAL PRIMARY KEY,
    agent_trip_id   BIGINT NOT NULL,
    image_url       VARCHAR(500) NOT NULL,
    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_agent_trip_images_trip
        FOREIGN KEY (agent_trip_id) REFERENCES agent_trips (id) ON DELETE CASCADE
);

CREATE INDEX idx_agent_trip_images_trip ON agent_trip_images (agent_trip_id);

-- ---------------------------------------------------------------------------
-- services (provider offerings: hotel, insurance, visa, custom)
-- ---------------------------------------------------------------------------
CREATE TABLE services (
    id              BIGSERIAL PRIMARY KEY,
    provider_id     BIGINT NOT NULL,
    service_type    VARCHAR(30) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price           DECIMAL(10, 2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    meta            JSON,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_services_type
        CHECK (service_type IN ('HOTEL_ROOM', 'INSURANCE_PLAN', 'VISA_SERVICE', 'CUSTOM')),
    CONSTRAINT ck_services_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT fk_services_provider
        FOREIGN KEY (provider_id) REFERENCES users (id)
);

CREATE INDEX idx_services_provider ON services (provider_id);
CREATE INDEX idx_services_type ON services (service_type);
CREATE INDEX idx_services_status ON services (status);
CREATE INDEX idx_services_provider_type ON services (provider_id, service_type);
CREATE INDEX idx_services_provider_status ON services (provider_id, status);

-- ---------------------------------------------------------------------------
-- trip_services (add-on linkage: agent_trips ↔ services)
-- ---------------------------------------------------------------------------
CREATE TABLE trip_services (
    id              BIGSERIAL PRIMARY KEY,
    agent_trip_id   BIGINT NOT NULL,
    service_id      BIGINT NOT NULL,
    is_optional     BOOLEAN NOT NULL DEFAULT TRUE,
    override_price  DECIMAL(10, 2),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_trip_services_trip_service UNIQUE (agent_trip_id, service_id),
    CONSTRAINT fk_trip_services_trip
        FOREIGN KEY (agent_trip_id) REFERENCES agent_trips (id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_services_service
        FOREIGN KEY (service_id) REFERENCES services (id)
);

CREATE INDEX idx_trip_services_trip ON trip_services (agent_trip_id);
CREATE INDEX idx_trip_services_service ON trip_services (service_id);
