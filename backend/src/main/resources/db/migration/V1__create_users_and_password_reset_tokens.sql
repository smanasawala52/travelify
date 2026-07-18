-- Travelify Module 1: users + password_reset_tokens
-- Compatible with PostgreSQL and H2 (MODE=PostgreSQL)

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    phone           VARCHAR(30),
    role            VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    avatar_url      VARCHAR(512),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    last_login_at   TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'AGENT', 'ADMIN'))
);

CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_is_active ON users (is_active);

CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    token       VARCHAR(128) NOT NULL,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_password_reset_tokens_token UNIQUE (token)
);

CREATE INDEX idx_prt_email ON password_reset_tokens (email);
CREATE INDEX idx_prt_expires_at ON password_reset_tokens (expires_at);
