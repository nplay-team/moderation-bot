package de.nplay.moderationbot.config;

import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.auditlog.lifecycle.Lifecycle;
import de.nplay.moderationbot.auditlog.lifecycle.Service;
import de.nplay.moderationbot.auditlog.lifecycle.events.ConfigEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.Optional;

public class ConfigService extends Service {

    public ConfigService(Lifecycle lifecycle) {
        super(lifecycle);
    }

    public Optional<String> get(BotConfig config) {
        return Query.query("SELECT value FROM configs WHERE name = ?")
                .single(Call.of().bind(config))
                .mapAs(String.class)
                .first();
    }

    public void set(BotConfig config, String value, UserSnowflake issuer) {
        publish(new ConfigEvent(AuditlogType.CONFIG_UPDATE, issuer, config, get(config).orElse(""), value));

        Query.query("INSERT INTO configs (name, value) VALUES (?, ?) ON CONFLICT (name) DO UPDATE SET value = EXCLUDED.value")
                .single(Call.of().bind(config).bind(value))
                .insert();
    }

    public enum BotConfig {
        SPIELERSUCHE_AUSSCHLUSS_ROLLE,
        SERVERLOG_KANAL
    }
}
