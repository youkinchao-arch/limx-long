-- P2: equipment calibration + maintenance workflow (PostgreSQL)

ALTER TABLE equipment ADD COLUMN maintenance_date       DATE;
ALTER TABLE equipment ADD COLUMN maintenance_due        DATE;
ALTER TABLE equipment ADD COLUMN maintenance_cycle_days INTEGER;

CREATE TABLE equipment_records (
    id                BIGSERIAL PRIMARY KEY,
    equipment_id      BIGINT      NOT NULL,
    record_type       VARCHAR(32) NOT NULL,
    result            VARCHAR(32),
    performed_date    DATE,
    performed_by      BIGINT,
    performed_by_name VARCHAR(64),
    provider          VARCHAR(128),
    certificate_no    VARCHAR(128),
    next_due_date     DATE,
    cycle_days        INTEGER,
    signature_hash    VARCHAR(128),
    notes             TEXT,
    created_at        TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_equipment_records_equipment ON equipment_records (equipment_id);
