package de.nplay.moderationbot.config;

import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.config.bot.BotConfig;

import java.util.Optional;

public class ConfigService {

    public static Optional<String> get(BotConfig config) {
        return Query.query("SELECT value FROM configs WHERE name = ?")
                .single(Call.of().bind(config))
                .mapAs(java.lang.String.class)
                .first();
    }

    public static void set(BotConfig config, String value) {
        Query.query("INSERT INTO configs (name, value) VALUES (?, ?) ON CONFLICT (name) DO UPDATE SET value = EXCLUDED.value")
                .single(Call.of()
                        .bind(config)
                        .bind(value)
                )
                .insert();
    }

}
