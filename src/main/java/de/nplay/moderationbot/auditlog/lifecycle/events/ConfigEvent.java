package de.nplay.moderationbot.auditlog.lifecycle.events;

import de.nplay.moderationbot.auditlog.AuditlogService.UnresolvedSnowflake;
import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.UserSnowflake;

public record ConfigEvent(
        AuditlogType type,
        UserSnowflake issuer,
        BotConfig config,
        String oldValue,
        String newValue
) implements BotEvent {

    @Override
    public ISnowflake target() {
        return new UnresolvedSnowflake(0);
    }
}
