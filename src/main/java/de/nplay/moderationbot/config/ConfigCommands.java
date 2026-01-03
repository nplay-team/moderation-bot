package de.nplay.moderationbot.config;

import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

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
            embed.fields().add(new Field(config.toString(), value.orElse(event.resolve("no-value-set")), false));
        });

        event.with().embeds(embed).reply();
    }
}
