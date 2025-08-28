package de.nplay.moderationbot.permissions;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class BotPermissions {

    public static final String ADMINISTRATOR = "ADMINISTRATOR";
    public static final String MODERATION_READ = "MODERATION_READ";
    public static final String MODERATION_CREATE = "MODERATION_CREATE";
    public static final String MODERATION_REVERT = "MODERATION_REVERT";
    public static final String MODERATION_DELETE = "MODERATION_DELETE";
    public static final String PERMISSION_READ = "PERMISSION_READ";
    public static final String PERMISSION_MANAGE = "PERMISSION_MANAGE";

    /// Combines multiple permissions into one integer.
    public static int combine(Collection<Integer> permissions) {
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
        ADMINISTRATOR(1, "Administrator"),
        MODERATION_READ(1 << 1, "Einsehen von moderativen Handlung"),
        MODERATION_CREATE(1 << 2, "Moderieren von Benutzern"),
        MODERATION_REVERT(1 << 3, "Rückgängig machen von moderativen Handlungen"),
        MODERATION_DELETE(1 << 4, "Löschen von moderativen Handlungen"),
        PERMISSION_READ(1 << 5, "Einsehen von Berechtigungen"),
        PERMISSION_MANAGE(1 << 6, "Vergeben von Berechtigungen");

        public final int value;
        public final String displayName;

        BitFields(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
    }
}
