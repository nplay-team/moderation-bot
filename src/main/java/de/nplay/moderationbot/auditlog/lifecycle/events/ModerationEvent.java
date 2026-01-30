package de.nplay.moderationbot.auditlog.lifecycle.events;

import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import net.dv8tion.jda.api.entities.UserSnowflake;

public interface ModerationEvent extends BotEvent {

    record Create(
            AuditlogType type,
            UserSnowflake issuer,
            UserSnowflake target,
            ModerationAct act
    ) implements ModerationEvent { }

    record Delete(
            AuditlogType type,
            UserSnowflake issuer,
            UserSnowflake target,
            long act
    ) implements ModerationEvent { }

    record Revert(
            AuditlogType type,
            UserSnowflake issuer,
            UserSnowflake target,
            RevertedModerationAct act
    ) implements ModerationEvent { }

}
