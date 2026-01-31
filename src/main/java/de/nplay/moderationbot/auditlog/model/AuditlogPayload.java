package de.nplay.moderationbot.auditlog.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.lifecycle.events.MessagePurgeEvent;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.moderation.MessageReferenceService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.notes.NotesService.Note;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public sealed interface AuditlogPayload {

    Logger log = LoggerFactory.getLogger(AuditlogPayload.class);
    ObjectMapper objectMapper = new ObjectMapper();

    static Optional<AuditlogPayload> fromJson(AuditlogType type, @Nullable InputStream json) {
        if (json == null || type.payloadType() == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type.payloadType()));
        } catch (IOException e) {
            log.error("Failed to deserialize AuditlogPayload", e);
            return Optional.empty();
        }
    }

    static Optional<String> toJson(AuditlogPayload payload) {
        try {
            return Optional.ofNullable(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize AuditlogPayload", e);
            return Optional.empty();
        }
    }

    record PermissionsUpdate(int oldPermissions, int newPermissions) implements AuditlogPayload { }

    record ConfigUpdate(BotConfig config, String oldValue, String newValue) implements AuditlogPayload { }

    record NotePayload(
            long id,
            long issuerId,
            long targetId,
            String content,
            long createdAt
    ) implements AuditlogPayload {

        public NotePayload(Note note) {
            this(note.id(), note.issuer().getIdLong(), note.target().getIdLong(), note.content(), note.createdAt().millis());
        }
    }

    record SlowmodePayload(long duration) implements AuditlogPayload { }

    record ModerationCreate(
            long id,
            long issuerId,
            long targetId,
            String reason,
            @Nullable Integer paragraphId,
            @Nullable Long messageReferenceId,
            @Nullable Long revokeAt,
            long duration
    ) implements AuditlogPayload {

        public ModerationCreate(ModerationAct act) {
            this(
                    act.id(),
                    act.issuer().getIdLong(),
                    act.user().getIdLong(),
                    act.reason(),
                    act.paragraph().map(RuleService.RuleParagraph::id).orElse(null),
                    act.messageReference().map(MessageReferenceService.MessageReference::messageId).orElse(null),
                    act.revokeAt().map(Replies.RelativeTime::millis).orElse(null),
                    act.duration().toMillis()
            );
        }
    }

    record ModerationRevert(
            long id,
            long issuerId,
            long targetId,
            String reason,
            @Nullable Integer paragraphId,
            @Nullable Long messageReferenceId,
            long revertedBy,
            String revertingReason,
            boolean automatic
    ) implements AuditlogPayload {

        public ModerationRevert(RevertedModerationAct act, boolean automatic) {
            this(
                    act.id(),
                    act.issuer().getIdLong(),
                    act.user().getIdLong(),
                    act.reason(),
                    act.paragraph().map(RuleService.RuleParagraph::id).orElse(null),
                    act.messageReference().map(MessageReferenceService.MessageReference::messageId).orElse(null),
                    act.revertedBy().getIdLong(),
                    act.revertingReason(),
                    automatic
            );
        }
    }

    record ModerationDelete(
            long id,
            long issuerId,
            long targetId,
            String reason,
            @Nullable Integer paragraphId,
            @Nullable Long messageReferenceId,
            @Nullable Long revokeAt,
            long duration,
            long deletedBy
    ) implements AuditlogPayload {

        public ModerationDelete(ModerationAct act, UserSnowflake deletedBy) {
            this(
                    act.id(),
                    act.issuer().getIdLong(),
                    act.user().getIdLong(),
                    act.reason(),
                    act.paragraph().map(RuleService.RuleParagraph::id).orElse(null),
                    act.messageReference().map(MessageReferenceService.MessageReference::messageId).orElse(null),
                    act.revokeAt().map(Replies.RelativeTime::millis).orElse(null),
                    act.duration().toMillis(),
                    deletedBy.getIdLong()
            );
        }
    }

    record MessagePurge(
            long issuerId,
            long targetId,
            long pivotMessageId,
            @Nullable Integer amount
    ) implements AuditlogPayload {

        public MessagePurge(MessagePurgeEvent event) {
            this(event.issuer().getIdLong(), event.target().getIdLong(), event.pivotMessageId(), event.amount());
        }
    }
}
