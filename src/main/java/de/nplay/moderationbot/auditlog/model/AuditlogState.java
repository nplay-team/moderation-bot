package de.nplay.moderationbot.auditlog.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public sealed interface AuditlogState {

    Logger log = LoggerFactory.getLogger(AuditlogState.class);
    ObjectMapper objectMapper = new ObjectMapper();

    static Optional<AuditlogState> fromJson(AuditlogType type, String json) {
        try {
            return switch (type) {
                case PERMISSIONS_USER_UPDATE, PERMISSIONS_ROLE_UPDATE ->
                        Optional.of(objectMapper.readValue(json, PermissionsUpdate.class));
                case CONFIG_UPDATE -> Optional.of(objectMapper.readValue(json, ConfigUpdate.class));
                default -> Optional.empty();
            };
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize AuditlogState", e);
            return Optional.empty();
        }
    }

    static Optional<String> toJson(AuditlogState state) {
        try {
            return Optional.ofNullable(objectMapper.writeValueAsString(state));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize AuditlogState", e);
            return Optional.empty();
        }
    }

    record PermissionsUpdate(int permission) implements AuditlogState { }

    record ConfigUpdate(BotConfig config, String value) implements AuditlogState { }
}
