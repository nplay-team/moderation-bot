package de.nplay.moderationbot.moderation;

public enum ModerationActType {
    WARN("Verwarnung"),
    TIMEOUT("Timeout"),
    KICK("Kick"),
    TEMP_BAN("Temporärer Ban"),
    BAN("Ban");

    public final String humanReadableString;

    ModerationActType(String humanReadableString) {
        this.humanReadableString = humanReadableString;
    }
}
