package de.nplay.moderationbot.config.bot;

import de.nplay.moderationbot.config.bot.type.BotConfigTypes;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

public class BotConfigs {

    public static BotConfig<Role> SpielersucheAusschlussRolle(@NotNull JDA jda) {
        return new BotConfig<>("spielersucheAusschlussRolle", BotConfigTypes.ROLE(), it -> BotConfigDefaultValidators.isRole(it, jda));
    }

}
