package de.nplay.moderationbot.config;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.config.bot.BotConfig;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
@Permissions(BotPermissions.ADMINISTRATOR)
@CommandConfig(enabledFor = Permission.ADMINISTRATOR)
public class ConfigCommands {

    @Command(value = "config set spielersuche-ausschluss-rolle", desc = "Setzt eine Konfigurations-Variable")
    public void setSpielersucheAusschlussRolle(CommandEvent event,
                                               @Param("Die Rolle, welche der User erhalten soll, wenn er von der Spielersuche ausgeschlossen wird.")
                                               Role role) {
        onConfigSet(event, BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE, role.getId());
    }

    @Command(value = "config set serverlog-kanal", desc = "Setzt eine Konfigurations-Variable")
    public void setConfig(CommandEvent event,
                          @Param("Der Text-Kanal in denen die Bot-Logs gesendet werden sollen.") TextChannel channel) {
        onConfigSet(event, BotConfig.SERVERLOG_KANAL, channel.getId());
    }

    void onConfigSet(CommandEvent event, BotConfig config, String value) {
        ConfigService.set(config, value);
        event.with().embeds("configSet", entry("key", config)).reply();
    }

    @Command(value = "config list", desc = "Listet alle Konfigurations-Variablen auf")
    public void listConfig(CommandEvent event) {
        var configs = BotConfig.getConfigs();

        var embed = event.embed("configList");

        configs.forEach(config -> {
            var value = ConfigService.get(config);
            embed.addField(config.toString(), value.orElse("Nicht gesetzt"), false);
        });

        event.jdaEvent().replyEmbeds(embed.build()).queue();
    }

}
