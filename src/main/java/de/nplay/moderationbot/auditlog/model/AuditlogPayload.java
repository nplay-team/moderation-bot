package de.nplay.moderationbot.auditlog.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.notes.NotesService.Note;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public sealed interface AuditlogPayload {

    Logger log = LoggerFactory.getLogger(AuditlogPayload.class);
    ObjectMapper objectMapper = new ObjectMapper();

    static Optional<AuditlogPayload> fromJson(AuditlogType type, @Nullable String json) {
        if (json == null || type.payloadType() == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type.payloadType()));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize AuditlogPayload", e);
            return Optional.empty();
        }
    }

    static Optional<String> toJson(AuditlogPayload state) {
        try {
            return Optional.ofNullable(objectMapper.writeValueAsString(state));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize AuditlogPayload", e);
            return Optional.empty();
        }
    }

    record PermissionsUpdate(int oldPermissions, int newPermissions) implements AuditlogPayload { }

    record ConfigUpdate(BotConfig config, String oldValue, String newValue) implements AuditlogPayload { }

    record NotePayload(long id, long issuerId, long targetId, String content, long createdAt) implements AuditlogPayload {

        public NotePayload(Note note) {
            this(note.id(), note.issuer().getIdLong(), note.target().getIdLong(), note.content(), note.createdAt().millis());
        }
    }
}
