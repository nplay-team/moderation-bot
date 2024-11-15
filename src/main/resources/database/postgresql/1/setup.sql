-- create types
CREATE TYPE REPORTACTION AS ENUM ('WARN', 'TIMEOUT', 'KICK', 'TEMP_BAN', 'BAN');

/**
  OPENED - The report is created
  DONE - All pending actions (like unbanning after validUntil) are executed
  REVERTED - The report got reverted / is not valid anymore
 */
CREATE TYPE REPORTSTATUS AS ENUM ('OPENED', 'DONE', 'REVERTED');

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
    number TEXT,
    title TEXT,
    content TEXT
);

CREATE TABLE moderations (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    userId BIGINT NOT NULL,
    guildId BIGINT NOT NULL,
    action REPORTACTION NOT NULL,
    status REPORTSTATUS NOT NULL DEFAULT 'OPENED',
    
    reason TEXT,
    paragraphId INT,
    referenceMessage TEXT,
    validUntil DATE,
    
    issuerId BIGINT,
    
    CONSTRAINT fkey_moderations_userId FOREIGN KEY (userId) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fkey_moderations_paragraphId FOREIGN KEY (paragraphId) REFERENCES rule_paragraphs (id) ON DELETE SET NULL,
    CONSTRAINT fkey_moderations_issuerId FOREIGN KEY (issuerId) REFERENCES users (id) ON DELETE SET NULL
);
