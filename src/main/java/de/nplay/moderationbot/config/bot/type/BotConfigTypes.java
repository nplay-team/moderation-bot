package de.nplay.moderationbot.config.bot.type;

public class BotConfigTypes {

    public static BotConfigType ROLE() {
        return new BotConfigType("Rolle", it -> "<@&%s> (%s)".formatted(it, it));
    }

    public static BotConfigType USER() {
        return new BotConfigType("User", it -> "<@!%s> (%s)".formatted(it, it));
    }

    public static BotConfigType CHANNEL() {
        return new BotConfigType("Channel", it -> "<#%s> (%s)".formatted(it, it));
    }

    public static BotConfigType STRING() {
        return new BotConfigType("String", it -> it);
    }

}
