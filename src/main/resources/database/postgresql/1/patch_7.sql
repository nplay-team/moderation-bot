UPDATE moderations SET reason = '' WHERE reason IS NULL;
ALTER table moderations ALTER column reason SET NOT NULL;