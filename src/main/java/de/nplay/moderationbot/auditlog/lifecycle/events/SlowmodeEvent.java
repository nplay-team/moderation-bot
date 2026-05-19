package de.nplay.moderationbot.auditlog.lifecycle.events;

import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

public record SlowmodeEvent(
        AuditlogType type,
        UserSnowflake issuer,
        GuildChannel target,
        @Nullable Duration duration
) implements BotEvent {

    public long durationMillis() {
        return duration == null ? 0 : duration.toMillis();
    }
}
