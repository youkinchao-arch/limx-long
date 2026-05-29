-- P1: audit trail + account security hardening (PostgreSQL)

ALTER TABLE users ADD COLUMN token_version          INTEGER     NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN failed_login_attempts  INTEGER     NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN locked_until           TIMESTAMPTZ;

CREATE TABLE audit_logs (
    id             BIGSERIAL PRIMARY KEY,
    actor_id       BIGINT,
    actor_username VARCHAR(64),
    action         VARCHAR(32) NOT NULL,
    entity_type    VARCHAR(64),
    entity_id      VARCHAR(64),
    before_value   TEXT,
    after_value    TEXT,
    ip             VARCHAR(64),
    created_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_logs_created_at  ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_entity      ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor       ON audit_logs (actor_id);
