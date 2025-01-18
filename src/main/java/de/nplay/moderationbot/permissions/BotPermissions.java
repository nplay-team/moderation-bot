package de.nplay.moderationbot.permissions;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class BotPermissions {

    public static final String MODERATION_READ = "MODERATION_READ";
    public static final String MODERATION_CREATE = "MODERATION_CREATE";
    public static final String MODERATION_DELETE = "MODERATION_DELETE";
    public static final String MODLOG_READ = "MODLOG_READ";
    public static final String BAN_APPEAL_MANAGE = "BAN_APPEAL_MANAGE";
    public static final String PERMISSION_READ = "PERMISSION_READ";
    public static final String PERMISSION_MANAGE = "PERMISSION_MANAGE";
    public static final String PARAGRAPH_MANAGE = "PARAGRAPH_MANAGE";
    public static final String ADMINISTRATOR = "ADMINISTRATOR";

    /// Combines multiple permissions into one integer.
    public static int combine(@NotNull Collection<Integer> permissions) {
        int combined = 0;
        for (int permission : permissions) {
            combined |= permission;
        }
        return combined;
    }

    /// Decodes the given permission integer to a collection of [BitFields]
    @NotNull
    public static Collection<BitFields> decode(int permissions) {
        return Arrays.stream(BitFields.values())
                .filter(it -> (permissions & it.value) != 0)
                .toList();
    }

    public enum BitFields {
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
        public final String displayName;

        BitFields(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
    }
}
