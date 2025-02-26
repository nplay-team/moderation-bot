package de.nplay.moderationbot.config.bot;

import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.bot.type.BotConfigType;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class BotConfig {

    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);
    private final String key;
    private final String displayName;
    private final BotConfigType type;

    BotConfig(String key, String displayName, BotConfigType type) {
        this.key = key;
        this.displayName = displayName;
        this.type = type;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public Optional<String> value() {
        return ConfigService.get(key);
    }

    public Optional<String> formattedValue() {
        return value().map(type.formatter());
    }

    public BotConfigType type() {
        return type;
    }

    public Boolean validate(String value) {
        return type.validator().apply(value);
    }

    public static Collection<BotConfig> getConfigs(JDA jda) {
        return Arrays.stream(BotConfigs.class.getMethods())
                .filter(method -> method.getReturnType().equals(BotConfig.class))
                .map(method -> {
                    try {
                        return (BotConfig) method.invoke(null, jda);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static Optional<BotConfig> getConfig(String name, JDA jda) {
        return getConfigs(jda).stream()
                .filter(config -> config.key().equalsIgnoreCase(name))
                .findAny();
    }

}
