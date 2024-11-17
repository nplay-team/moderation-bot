package de.nplay.moderationbot.permissions;

public enum BotPermissionBitfield {
    MODERATION_READ(1, "Einsehen von moderativen Aktionen"),
    MODERATION_CREATE(1 << 1, "Moderieren von Benutzern"),
    MODERATION_DELETE(1 << 2, "Löschen/Revidieren von moderativen Handlungen"),
    MODLOG_READ(1 << 3, "Einsehen des Modlogs"),
    BAN_APPEAL_MANAGE(1 << 4, "Verwalten von Entbannungsanträgen"),
    PERMISSION_READ(1 << 5, "Einsehen von Berechtigungen"),
    PERMISSION_MANAGE(1 << 6, "Vergeben von Berechtigungen"),
    PARAGRAPH_MANAGE(1 << 7, "Verwalten von Regelparagraphen"),
    ADMINISTRATOR(1 << 8, "Administrator");
    
    public final int value;
    public final String humanReadableName;

    BotPermissionBitfield(int value, String humanReadableName) {
        this.value = value;
        this.humanReadableName = humanReadableName;
    }
}
