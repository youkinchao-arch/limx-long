-- P2: report approval + e-signature + issue workflow (PostgreSQL)

ALTER TABLE reports ADD COLUMN conclusion        TEXT;
ALTER TABLE reports ADD COLUMN submitted_by      BIGINT;
ALTER TABLE reports ADD COLUMN submitted_by_name VARCHAR(64);
ALTER TABLE reports ADD COLUMN submitted_at      TIMESTAMPTZ;
ALTER TABLE reports ADD COLUMN approved_by       BIGINT;
ALTER TABLE reports ADD COLUMN approved_by_name  VARCHAR(64);
ALTER TABLE reports ADD COLUMN approved_at       TIMESTAMPTZ;
ALTER TABLE reports ADD COLUMN signed_by         BIGINT;
ALTER TABLE reports ADD COLUMN signed_by_name    VARCHAR(64);
ALTER TABLE reports ADD COLUMN signed_at         TIMESTAMPTZ;
ALTER TABLE reports ADD COLUMN signature_hash    VARCHAR(128);
ALTER TABLE reports ADD COLUMN issued_by         BIGINT;
ALTER TABLE reports ADD COLUMN issued_by_name    VARCHAR(64);
ALTER TABLE reports ADD COLUMN issued_at         TIMESTAMPTZ;

CREATE TABLE report_signatures (
    id             BIGSERIAL PRIMARY KEY,
    report_id      BIGINT      NOT NULL,
    action         VARCHAR(32) NOT NULL,
    from_status    VARCHAR(32),
    to_status      VARCHAR(32),
    actor_id       BIGINT,
    actor_username VARCHAR(64),
    meaning        VARCHAR(128),
    signature_hash VARCHAR(128),
    comment        TEXT,
    created_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_report_signatures_report ON report_signatures (report_id);
