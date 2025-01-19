ALTER TABLE moderations
    ADD COLUMN IF NOT EXISTS reverted_by BIGINT;
ALTER TABLE moderations
    ADD COLUMN IF NOT EXISTS reverted_at TIMESTAMP;
ALTER TABLE moderations
    ADD COLUMN IF NOT EXISTS revert_reason TEXT;
