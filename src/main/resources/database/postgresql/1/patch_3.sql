CREATE TABLE notes
(
    id         BIGSERIAL NOT NULL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    creator_id BIGINT    NOT NULL,
    content    TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notes_userId ON notes (user_id);