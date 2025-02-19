CREATE TABLE serverlog_channels
(
    id             BIGSERIAL NOT NULL PRIMARY KEY,
    channel_id     BIGINT    NOT NULL,
    exclude_events TEXT[]    NOT NULL DEFAULT '{}',
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_serverlog_channels_channelId ON serverlog_channels (channel_id);