package de.nplay.moderationbot.config;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.config.bot.BotConfig;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Interaction
@Permissions(BotPermissions.ADMINISTRATOR)
public class ConfigCommands {

    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "config set spielersuche-ausschluss-rolle", desc = "Setzt eine Konfigurations-Variable", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void setSpielersucheAusschlussRolle(CommandEvent event, @Param("Die Rolle, welche der User erhalten soll, wenn er von der Spielersuche ausgeschlossen wird.") Role role) {
        onConfigSet(event, BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE, role.getId());
    }

    @SlashCommand(value = "config set serverlog-kanal", desc = "Setzt eine Konfigurations-Variable", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void setConfig(CommandEvent event, @Param("Der Text-Kanal in denen die Bot-Logs gesendet werden sollen.") TextChannel channel) {
        onConfigSet(event, BotConfig.SERVERLOG_KANAL, channel.getId());
    }

    void onConfigSet(CommandEvent event, BotConfig config, String value) {
        ConfigService.set(config, value);
        event.reply(embedCache.getEmbed("configSet").injectValue("key", config).injectValue("value", value).injectValue("color", EmbedColors.SUCCESS));
    }

    @SlashCommand(value = "config list", desc = "Listet alle Konfigurations-Variablen auf", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void listConfig(CommandEvent event) {
        var configs = BotConfig.getConfigs();

        var embed = embedCache.getEmbed("configList").injectValue("color", EmbedColors.DEFAULT).toEmbedBuilder();

        configs.forEach(config -> {
            var value = ConfigService.get(config);
            embed.addField(config.toString(), value.orElse("Nicht gesetzt"), false);
        });

        event.jdaEvent().replyEmbeds(embed.build()).queue();
    }

}
