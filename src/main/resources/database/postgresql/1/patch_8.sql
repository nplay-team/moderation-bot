CREATE TYPE AUDITLOG_TYPE AS ENUM (
    'MODERATION_CREATE',
    'MODERATION_REVERT',
    'MODERATION_DELETE',
    'MODERATION_PURGE',
    'NOTE_CREATE',
    'NOTE_DELETE',
    'PERMISSIONS_USER_UPDATE',
    'PERMISSIONS_ROLE_UPDATE',
    'CONFIG_UPDATE',
    'SLOWMODE_UPDATE',
    'SPIELERSUCHE_AUSSCHLUSS',
    'SPIELERSUCHE_FREIGABE'
    );

CREATE TABLE auditlog
(
    id         BIGSERIAL     NOT NULL PRIMARY KEY,
    type       AUDITLOG_TYPE NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    issuer_id  BIGINT        NOT NULL,
    target_id  BIGINT        NOT NULL,
    payload    JSONB
);

CREATE INDEX idx_auditlog_query_issuer ON auditlog (issuer_id, created_at, type);
CREATE INDEX idx_auditlog_query_target ON auditlog (target_id, created_at, type);
