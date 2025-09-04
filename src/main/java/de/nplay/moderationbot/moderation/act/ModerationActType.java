package de.nplay.moderationbot.moderation.act;

public enum ModerationActType {
    WARN("Verwarnung"),
    TIMEOUT("Timeout"),
    KICK("Kick"),
    TEMP_BAN("Temporärer Bann"),
    BAN("Bann");

    private final String humanReadableString;

    ModerationActType(String humanReadableString) {
        this.humanReadableString = humanReadableString;
    }

    @Override
    public String toString() {
        return humanReadableString;
    }

    public boolean isBan() {
        return this == BAN || this == TEMP_BAN;
    }

    public boolean isTemp() {
        return this == TEMP_BAN || this == TIMEOUT;
    }
}
