package de.nplay.moderationbot.config;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.config.bot.BotConfig;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction("config")
@Permissions(BotPermissions.ADMINISTRATOR)
@CommandConfig(enabledFor = Permission.ADMINISTRATOR)
public class ConfigCommands {

    @Command("set spielersuche-ausschluss-rolle")
    public void setSpielersucheAusschlussRolle(CommandEvent event, Role role) {
        onConfigSet(event, BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE, role.getId());
    }

    @Command("set serverlog-kanal")
    public void setConfig(CommandEvent event, TextChannel channel) {
        onConfigSet(event, BotConfig.SERVERLOG_KANAL, channel.getId());
    }

    void onConfigSet(CommandEvent event, BotConfig config, String value) {
        ConfigService.set(config, value);
        event.with().embeds("configSet", entry("key", config)).reply();
    }

    @Command("list")
    public void listConfig(CommandEvent event) {
        var configs = BotConfig.getConfigs();

        var embed = event.embed("configList");

        configs.forEach(config -> {
            var value = ConfigService.get(config);
            embed.addField(config.toString(), value.orElse("Nicht gesetzt"), false);
        });

        event.with().embeds(embed).reply();
    }
}
