package de.nplay.moderationbot.auditlog.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public sealed interface AuditlogPayload {

    Logger log = LoggerFactory.getLogger(AuditlogPayload.class);
    ObjectMapper objectMapper = new ObjectMapper();

    static Optional<AuditlogPayload> fromJson(AuditlogType type, @Nullable String json) {
        if (json == null) {
            return Optional.empty();
        }
        try {
            return switch (type) {
                case PERMISSIONS_USER_UPDATE, PERMISSIONS_ROLE_UPDATE ->
                        Optional.of(objectMapper.readValue(json, PermissionsUpdate.class));
                case CONFIG_UPDATE -> Optional.of(objectMapper.readValue(json, ConfigUpdate.class));
                default -> Optional.empty();
            };
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

    record NoteCreate(long noteId) implements AuditlogPayload { }

}
