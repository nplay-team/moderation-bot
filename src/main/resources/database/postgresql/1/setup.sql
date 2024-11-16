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
    messageId BIGINT NOT NULL PRIMARY KEY,
    channelId BIGINT NOT NULL,
    content TEXT
);

CREATE TABLE moderations (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    userId BIGINT NOT NULL,
    type REPORTTYPE NOT NULL,
    reverted BOOLEAN,
    
    reason TEXT,
    paragraphId INT,
    
    referenceMessage BIGINT,
    
    revokeAt TIMESTAMP,
    duration BIGINT, -- in seconds
    
    issuerId BIGINT NOT NULL,
    
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fkey_moderations_paragraphId FOREIGN KEY (paragraphId) REFERENCES rule_paragraphs (id) ON DELETE SET NULL,
    CONSTRAINT fkey_moderations_referenceMessage FOREIGN KEY (referenceMessage) REFERENCES message_references (messageId) ON DELETE SET NULL
);
