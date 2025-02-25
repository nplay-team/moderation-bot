package de.nplay.moderationbot.config.bot.type;

import java.util.function.Function;

public record BotConfigType(String displayName, Function<String, String> formatter) {
}
