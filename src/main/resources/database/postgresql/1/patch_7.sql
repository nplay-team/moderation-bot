UPDATE moderations SET reason = '' WHERE reason IS NULL;
ALTER table moderations ALTER column reason SET NOT NULL;
UPDATE moderations SET revert_reason = '' WHERE revert_reason IS NULL;
ALTER table moderations ALTER column revert_reason SET NOT NULL;