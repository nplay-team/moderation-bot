package de.nplay.moderationbot.auditlog.model;

import de.nplay.moderationbot.auditlog.model.AuditlogPayload.*;
import org.jspecify.annotations.Nullable;

public enum AuditlogType {
    MODERATION_CREATE,
    MODERATION_REVERT,
    MODERATION_DELETE,
    MODERATION_PURGE,
    NOTE_CREATE(NoteCreate.class),
    NOTE_DELETE,
    PERMISSIONS_USER_UPDATE(PermissionsUpdate.class),
    PERMISSIONS_ROLE_UPDATE,
    CONFIG_UPDATE(ConfigUpdate.class),
    SLOWMODE_SET,
    SLOWMODE_REMOVE,
    SPIELERSUCHE_AUSSCHLUSS,
    SPIELERSUCHE_FREIGABE;

    private final @Nullable Class<? extends AuditlogPayload> payloadType;

    AuditlogType() {
        this(null);
    }

    AuditlogType(@Nullable Class<? extends AuditlogPayload> payloadType) {
        this.payloadType = payloadType;
    }

    public @Nullable Class<? extends AuditlogPayload> payloadType() {
        return payloadType;
    }
}
