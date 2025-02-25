package de.nplay.moderationbot.config.bot;

import de.nplay.moderationbot.config.bot.type.BotConfigTypes;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public class BotConfigs {

    public static BotConfig SpielersucheAusschlussRolle(@NotNull JDA jda) {
        return new BotConfig("spielersucheAusschlussRolle", BotConfigTypes.ROLE(jda));
    }

}
