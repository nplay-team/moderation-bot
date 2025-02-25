package de.nplay.moderationbot.config.bot.type;

import net.dv8tion.jda.api.JDA;

import java.util.Objects;

public class BotConfigTypes {

    public static BotConfigType ROLE(JDA jda) {
        return new BotConfigType("Rolle", it -> jda.getRoleById(it) != null, it -> "<@&%s> (%s)".formatted(it, it));
    }

    public static BotConfigType USER(JDA jda) {
        return new BotConfigType("User", it -> jda.getUserById(it) != null, it -> "<@!%s> (%s)".formatted(it, it));
    }

    public static BotConfigType CHANNEL(JDA jda) {
        return new BotConfigType("Channel", it -> jda.getTextChannelById(it) != null, it -> "<#%s> (%s)".formatted(it, it));
    }

    public static BotConfigType STRING() {
        return new BotConfigType("String", Objects::nonNull, it -> it);
    }

}
