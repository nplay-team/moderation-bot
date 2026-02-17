package de.nplay.moderationbot.auditlog.lifecycle.events;

import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jspecify.annotations.Nullable;

public record MessagePurgeEvent(
        UserSnowflake issuer,
        MessageChannel target,
        long pivotMessageId,
        @Nullable Integer amount
        ) implements BotEvent {

    @Override
    public AuditlogType type() {
        return AuditlogType.MESSAGE_PURGE;
    }
}
