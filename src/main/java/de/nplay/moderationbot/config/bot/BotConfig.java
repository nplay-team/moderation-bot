package de.nplay.moderationbot.config.bot;

import java.util.Arrays;
import java.util.Collection;

public enum BotConfig {
    SPIELERSUCHE_AUSSCHLUSS_ROLLE("spielersucheAusschlussRolle"),
    SERVERLOG_KANAL("serverlogKanal");

    private final String key;

    BotConfig(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

    public static Collection<String> getConfigs() {
        return Arrays.stream(BotConfig.values()).map(BotConfig::toString).toList();
    }
}
