package de.nplay.moderationbot.config;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.CommandConfig;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
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

    private void onConfigSet(CommandEvent event, BotConfig config, String value) {
        ConfigService.set(config, value);
        event.with().embeds("configSet", entry("key", config.toString()), entry("value", value)).reply();
    }

    @Command("list")
    public void listConfig(CommandEvent event) {
        var configs = BotConfig.configs();

        var embed = event.embed("configList");

        configs.forEach(config -> {
            var value = ConfigService.get(config);
            embed.fields().add(new Field(config.toString(), value.orElse(event.localize("no-value-set")), false));
        });

        event.with().embeds(embed).reply();
    }
}
