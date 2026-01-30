package de.nplay.moderationbot.auditlog.model;

import de.nplay.moderationbot.auditlog.model.AuditlogPayload.ConfigUpdate;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload.NotePayload;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload.PermissionsUpdate;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload.SlowmodePayload;
import org.jspecify.annotations.Nullable;

public enum AuditlogType {
    MODERATION_CREATE,
    MODERATION_REVERT,
    MODERATION_DELETE,
    MODERATION_PURGE,
    NOTE_CREATE(NotePayload.class),
    NOTE_DELETE(NotePayload.class),
    PERMISSIONS_USER_UPDATE(PermissionsUpdate.class),
    PERMISSIONS_ROLE_UPDATE(PermissionsUpdate.class),
    CONFIG_UPDATE(ConfigUpdate.class),
    SLOWMODE_UPDATE(SlowmodePayload.class),
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
