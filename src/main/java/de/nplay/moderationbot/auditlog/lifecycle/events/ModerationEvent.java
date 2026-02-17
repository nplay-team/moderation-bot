package de.nplay.moderationbot.auditlog.lifecycle.events;

import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.UserSnowflake;

public interface ModerationEvent extends BotEvent {

    ModerationAct act();

    @Override
    default UserSnowflake issuer() {
        return act().issuer();
    }

    @Override
    default ISnowflake target() {
        return act().user();
    }

    record Create(ModerationAct act) implements ModerationEvent {

        @Override
        public AuditlogType type() {
            return AuditlogType.MODERATION_CREATE;
        }
    }

    record Delete(ModerationAct act, UserSnowflake deletedBy) implements ModerationEvent {

        @Override
        public UserSnowflake issuer() {
            return deletedBy;
        }

        @Override
        public AuditlogType type() {
            return AuditlogType.MODERATION_DELETE;
        }
    }

    record Revert(RevertedModerationAct act, boolean automatic) implements ModerationEvent {

        @Override
        public AuditlogType type() {
            return AuditlogType.MODERATION_REVERT;
        }
    }
}
