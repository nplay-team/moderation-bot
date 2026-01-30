package de.nplay.moderationbot.auditlog.lifecycle.events;

import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import net.dv8tion.jda.api.entities.UserSnowflake;

public record SpielersucheFreigabeEvent(UserSnowflake issuer, UserSnowflake target) implements BotEvent {

    @Override
    public AuditlogType type() {
        return AuditlogType.SPIELERSUCHE_AUSSCHLUSS;
    }
}
