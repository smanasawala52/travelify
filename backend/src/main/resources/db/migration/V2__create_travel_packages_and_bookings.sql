-- Existing Module scaffolding: packages + bookings (required now that ddl-auto=validate)

CREATE TABLE travel_packages (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     VARCHAR(2000),
    destination     VARCHAR(255) NOT NULL,
    price           NUMERIC(12, 2) NOT NULL,
    duration_days   INTEGER NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_id   BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_travel_packages_created_by
        FOREIGN KEY (created_by_id) REFERENCES users (id)
);

CREATE INDEX idx_travel_packages_active ON travel_packages (active);
CREATE INDEX idx_travel_packages_destination ON travel_packages (destination);

CREATE TABLE bookings (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    package_id      BIGINT NOT NULL,
    travel_date     DATE NOT NULL,
    travelers       INTEGER NOT NULL,
    total_price     NUMERIC(12, 2) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_bookings_customer
        FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_package
        FOREIGN KEY (package_id) REFERENCES travel_packages (id),
    CONSTRAINT ck_bookings_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'))
);

CREATE INDEX idx_bookings_customer ON bookings (customer_id);
CREATE INDEX idx_bookings_package ON bookings (package_id);
CREATE INDEX idx_bookings_status ON bookings (status);
