-- create types
CREATE TYPE REPORTTYPE AS ENUM ('WARN', 'TIMEOUT', 'KICK', 'TEMP_BAN', 'BAN');

-- create tables
CREATE TABLE users (
    id BIGINT NOT NULL PRIMARY KEY,
    permissions INT DEFAULT 0
);

CREATE TABLE roles (
    id BIGINT NOT NULL PRIMARY KEY,
    permissions INT DEFAULT 0
);

CREATE TABLE rule_paragraphs (
    id SERIAL NOT NULL PRIMARY KEY,
    number TEXT NOT NULL,
    title TEXT NOT NULL,
    content TEXT
);

CREATE TABLE message_references (
    message_id BIGINT NOT NULL PRIMARY KEY,
    channel_id BIGINT NOT NULL,
    content TEXT
);

CREATE TABLE moderations (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type REPORTTYPE NOT NULL,
    reverted BOOLEAN,
    
    reason TEXT,
    paragraph_id INT,
    
    reference_message BIGINT,
    
    revoke_at TIMESTAMP,
    duration BIGINT, -- in millis
    
    issuer_id BIGINT NOT NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fkey_moderations_paragraphId FOREIGN KEY (paragraph_id) REFERENCES rule_paragraphs (id) ON DELETE SET NULL,
    CONSTRAINT fkey_moderations_referenceMessage FOREIGN KEY (reference_message) REFERENCES message_references (message_id) ON DELETE SET NULL
);
