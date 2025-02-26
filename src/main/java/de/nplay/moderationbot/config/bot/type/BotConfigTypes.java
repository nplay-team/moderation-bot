package de.nplay.moderationbot.config.bot.type;

import net.dv8tion.jda.api.JDA;

import java.util.Objects;

public class BotConfigTypes {

    private static boolean isSnowflake(String str) {
        return str.chars().allMatch(Character::isDigit);
    }

    public static BotConfigType ROLE(JDA jda) {
        return new BotConfigType("Rolle", it -> isSnowflake(it) && jda.getRoleById(it) != null, it -> "<@&%s> (%s)".formatted(it, it));
    }

    public static BotConfigType USER(JDA jda) {
        return new BotConfigType("User", it -> isSnowflake(it) && jda.getUserById(it) != null, it -> "<@!%s> (%s)".formatted(it, it));
    }

    public static BotConfigType TEXT_CHANNEL(JDA jda) {
        return new BotConfigType("Channel", it -> isSnowflake(it) && jda.getTextChannelById(it) != null, it -> "<#%s> (%s)".formatted(it, it));
    }

    public static BotConfigType STRING() {
        return new BotConfigType("String", Objects::nonNull, it -> it);
    }

}
