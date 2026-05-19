package de.nplay.moderationbot.auditlog.lifecycle;

import de.nplay.moderationbot.auditlog.model.AuditlogType;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.UserSnowflake;

public interface BotEvent {

    AuditlogType type();

    UserSnowflake issuer();

    ISnowflake target();

}
