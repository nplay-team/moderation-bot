CREATE TABLE trap_channels (
    channel_id  BIGINT PRIMARY KEY,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);