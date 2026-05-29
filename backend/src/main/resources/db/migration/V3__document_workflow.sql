-- P2: controlled document approval workflow (PostgreSQL)

ALTER TABLE documents ADD COLUMN submitted_by      BIGINT;
ALTER TABLE documents ADD COLUMN submitted_by_name VARCHAR(64);
ALTER TABLE documents ADD COLUMN submitted_at      TIMESTAMPTZ;
ALTER TABLE documents ADD COLUMN approved_by       BIGINT;
ALTER TABLE documents ADD COLUMN approved_by_name  VARCHAR(64);
ALTER TABLE documents ADD COLUMN approved_at       TIMESTAMPTZ;

CREATE TABLE document_reviews (
    id             BIGSERIAL PRIMARY KEY,
    document_id    BIGINT      NOT NULL,
    action         VARCHAR(32) NOT NULL,
    from_status    VARCHAR(32),
    to_status      VARCHAR(32),
    actor_id       BIGINT,
    actor_username VARCHAR(64),
    comment        TEXT,
    created_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_document_reviews_document ON document_reviews (document_id);
