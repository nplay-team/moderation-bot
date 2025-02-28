package de.nplay.moderationbot.config;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Choices;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.config.bot.BotConfig;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;

@Interaction
@Permissions(BotPermissions.ADMINISTRATOR)
public class ConfigCommands {

    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "config set", desc = "Setzt eine Konfigurations-Variable", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void setConfig(CommandEvent event, @Choices({"spielersucheAusschlussRolle", "serverlogKanal"}) String name, String value) {
        ConfigService.set(name, value);
        event.reply(embedCache.getEmbed("configSet").injectValue("key", name).injectValue("value", value).injectValue("color", EmbedColors.SUCCESS));
    }

    @SlashCommand(value = "config get", desc = "Gibt den Wert einer Konfigurations-Variable zurück", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void getConfig(CommandEvent event, @Choices({"spielersucheAusschlussRolle", "serverlogKanal"}) String name) {
        var botConfig = ConfigService.get(name);

        if (botConfig.isEmpty()) {
            event.reply(embedCache.getEmbed("configNotSet").injectValue("key", name).injectValue("color", EmbedColors.ERROR));
            return;
        }

        event.reply(
                embedCache.getEmbed("configGet")
                        .injectValue("key", name)
                        .injectValue("value", name)
                        .injectValue("color", EmbedColors.DEFAULT)
        );
    }

    @SlashCommand(value = "config list", desc = "Listet alle Konfigurations-Variablen auf", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void listConfig(CommandEvent event) {
        var configs = BotConfig.getConfigs();

        var embed = embedCache.getEmbed("configList").injectValue("color", EmbedColors.DEFAULT).toEmbedBuilder();

        configs.forEach(config -> {
            var value = ConfigService.get(config);
            embed.addField(config, value.orElse("Nicht gesetzt"), false);
        });

        event.jdaEvent().replyEmbeds(embed.build()).queue();
    }

}
