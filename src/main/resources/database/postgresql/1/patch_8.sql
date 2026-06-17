DO $$
DECLARE
	botId moderations.reverted_by%TYPE;
BEGIN
	-- we detect the bot id by finding the most occurring reverted_by id (should be the bot)
	SELECT reverted_by
	INTO botId
	FROM moderations
	WHERE reverted_by IS NOT NULL
	GROUP BY reverted_by
	ORDER BY count(*) desc, reverted_by
	LIMIT 1;

	RAISE INFO 'Detected botId: %', botId;

	UPDATE moderations
	SET reverted_by = botId
	WHERE reverted_by IS NULL;
END;
$$;

ALTER TABLE moderations ALTER reverted_by SET NOT NULL;