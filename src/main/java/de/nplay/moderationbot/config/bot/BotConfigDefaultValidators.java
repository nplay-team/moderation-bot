package de.nplay.moderationbot.config.bot;

import net.dv8tion.jda.api.JDA;

public class BotConfigDefaultValidators {

    public static boolean notNull(String value) {
        return value != null;
    }

    public static boolean isRole(String value, JDA jda) {
        return jda.getRoleById(value) != null;
    }

    public static boolean isChannel(String value, JDA jda) {
        return jda.getTextChannelById(value) != null;
    }

    public static boolean isUser(String value, JDA jda) {
        return jda.getUserById(value) != null;
    }

}
