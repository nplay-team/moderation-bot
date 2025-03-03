package de.nplay.moderationbot.config.bot;

import java.util.Arrays;
import java.util.Collection;

public enum BotConfig {
    SPIELERSUCHE_AUSSCHLUSS_ROLLE(),
    SERVERLOG_KANAL();

    public static Collection<BotConfig> getConfigs() {
        return Arrays.stream(BotConfig.values()).toList();
    }
}
