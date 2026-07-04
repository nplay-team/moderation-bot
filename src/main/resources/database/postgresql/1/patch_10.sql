CREATE TABLE trap_channels (
    channel_id  TEXT PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);