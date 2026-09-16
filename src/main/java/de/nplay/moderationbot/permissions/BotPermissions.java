package de.nplay.moderationbot.permissions;


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
    public static Collection<BitFields> decode(int permissions) {
        return Arrays.stream(BitFields.values())
                .filter(it -> (permissions & it.value) != 0)
                .toList();
    }

    public enum BitFields {
        ADMINISTRATOR(1, "admin"),
        MODERATION_READ(1 << 1, "mod-read"),
        MODERATION_CREATE(1 << 2, "mod-create"),
        MODERATION_REVERT(1 << 3, "mod-revert"),
        MODERATION_DELETE(1 << 4, "mod-delete"),
        PERMISSION_READ(1 << 5, "perm-read"),
        PERMISSION_MANAGE(1 << 6, "perm-manage");

        public final int value;
        public final String localizationKey;

        BitFields(int value, String localizationKey) {
            this.value = value;
            this.localizationKey = localizationKey;
        }
    }
}
