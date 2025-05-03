CREATE TABLE slowmode_channels
(
    channel_id BIGINT PRIMARY KEY,
    duration INT NOT NULL -- seconds
);