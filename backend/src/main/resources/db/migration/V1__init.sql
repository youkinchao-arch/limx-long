-- Baseline schema for Hongqiao LIMS (PostgreSQL)

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(64)   NOT NULL UNIQUE,
    name        VARCHAR(128)  NOT NULL,
    description VARCHAR(255),
    permissions VARCHAR(2000) NOT NULL DEFAULT '',
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL UNIQUE,
    full_name       VARCHAR(128) NOT NULL,
    email           VARCHAR(128),
    hashed_password VARCHAR(255) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    is_superuser    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE departments (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(128) NOT NULL,
    code       VARCHAR(64),
    parent_id  BIGINT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

CREATE TABLE personnel (
    id                   BIGSERIAL PRIMARY KEY,
    employee_no          VARCHAR(64)  NOT NULL UNIQUE,
    name                 VARCHAR(128) NOT NULL,
    gender               VARCHAR(16),
    department_id        BIGINT REFERENCES departments (id),
    position             VARCHAR(64),
    phone                VARCHAR(32),
    email                VARCHAR(128),
    hire_date            DATE,
    qualification        VARCHAR(255),
    qualification_expiry DATE,
    status               VARCHAR(32)  NOT NULL DEFAULT 'active',
    remark               TEXT,
    created_at           TIMESTAMPTZ,
    updated_at           TIMESTAMPTZ
);

CREATE TABLE training_records (
    id           BIGSERIAL PRIMARY KEY,
    personnel_id BIGINT       NOT NULL REFERENCES personnel (id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    category     VARCHAR(64),
    train_date   DATE,
    result       VARCHAR(64),
    score        NUMERIC(5, 2),
    remark       TEXT,
    created_at   TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ
);

CREATE TABLE equipment (
    id                     BIGSERIAL PRIMARY KEY,
    asset_no               VARCHAR(64)  NOT NULL UNIQUE,
    name                   VARCHAR(128) NOT NULL,
    category               VARCHAR(64),
    model                  VARCHAR(128),
    manufacturer           VARCHAR(128),
    serial_no              VARCHAR(128),
    location               VARCHAR(128),
    status                 VARCHAR(32)  NOT NULL DEFAULT 'idle',
    purchase_date          DATE,
    calibration_date       DATE,
    calibration_due        DATE,
    calibration_cycle_days INTEGER,
    remark                 TEXT,
    created_at             TIMESTAMPTZ,
    updated_at             TIMESTAMPTZ
);

CREATE TABLE suppliers (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(128) NOT NULL,
    contact    VARCHAR(128),
    phone      VARCHAR(32),
    remark     TEXT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

CREATE TABLE materials (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(64)  NOT NULL UNIQUE,
    name         VARCHAR(128) NOT NULL,
    category     VARCHAR(64),
    unit         VARCHAR(32),
    warehouse    VARCHAR(128),
    quantity     NUMERIC(14, 3),
    safety_stock NUMERIC(14, 3),
    expiry_date  DATE,
    remark       TEXT,
    created_at   TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ
);

CREATE TABLE documents (
    id             BIGSERIAL PRIMARY KEY,
    doc_no         VARCHAR(64)  NOT NULL UNIQUE,
    title          VARCHAR(255) NOT NULL,
    category       VARCHAR(64),
    version        VARCHAR(32),
    status         VARCHAR(32)  NOT NULL DEFAULT 'draft',
    effective_date DATE,
    remark         TEXT,
    created_at     TIMESTAMPTZ,
    updated_at     TIMESTAMPTZ
);

CREATE TABLE environment_records (
    id          BIGSERIAL PRIMARY KEY,
    location    VARCHAR(128) NOT NULL,
    temperature NUMERIC(6, 2),
    humidity    NUMERIC(6, 2),
    recorded_at TIMESTAMPTZ  NOT NULL,
    status      VARCHAR(32)  NOT NULL DEFAULT 'normal',
    remark      TEXT,
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);

CREATE TABLE test_methods (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    standard    VARCHAR(255),
    category    VARCHAR(64),
    limit_value VARCHAR(255),
    status      VARCHAR(32)  NOT NULL DEFAULT 'draft',
    remark      TEXT,
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);

CREATE TABLE reports (
    id         BIGSERIAL PRIMARY KEY,
    report_no  VARCHAR(64)  NOT NULL UNIQUE,
    title      VARCHAR(255) NOT NULL,
    customer   VARCHAR(128),
    template   VARCHAR(64),
    status     VARCHAR(32)  NOT NULL DEFAULT 'draft',
    issue_date DATE,
    remark     TEXT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

CREATE TABLE resource_bookings (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    resource_type VARCHAR(32)  NOT NULL,
    resource_name VARCHAR(128),
    start_time    TIMESTAMPTZ  NOT NULL,
    end_time      TIMESTAMPTZ  NOT NULL,
    priority      INTEGER      NOT NULL DEFAULT 0,
    status        VARCHAR(32)  NOT NULL DEFAULT 'pending',
    remark        TEXT,
    created_at    TIMESTAMPTZ,
    updated_at    TIMESTAMPTZ
);

CREATE INDEX idx_personnel_qualification_expiry ON personnel (qualification_expiry);
CREATE INDEX idx_equipment_calibration_due ON equipment (calibration_due);
CREATE INDEX idx_equipment_status ON equipment (status);
CREATE INDEX idx_materials_expiry_date ON materials (expiry_date);
CREATE INDEX idx_environment_status ON environment_records (status);
CREATE INDEX idx_training_personnel ON training_records (personnel_id);
