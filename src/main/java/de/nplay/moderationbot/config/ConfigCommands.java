package de.nplay.moderationbot.config;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("config")
@Interaction("config")
@Permissions(BotPermissions.ADMINISTRATOR)
@CommandConfig(enabledFor = Permission.ADMINISTRATOR)
public class ConfigCommands {

    private final ConfigService configService;

    @Inject
    public ConfigCommands(ConfigService configService) {
        this.configService = configService;
    }

    @Command("set spielersuche-ausschluss-rolle")
    public void setSpielersucheAusschlussRolle(CommandEvent event, Role role) {
        onConfigSet(event, BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE, role.getId());
    }

    @Command("set serverlog-kanal")
    public void setConfig(CommandEvent event, TextChannel channel) {
        onConfigSet(event, BotConfig.SERVERLOG_KANAL, channel.getId());
    }

    private void onConfigSet(CommandEvent event, BotConfig config, String value) {
        configService.set(config, value);
        event.reply(Replies.success("config-update"), entry("key", config.toString()), entry("value", value));
    }

    @Command("list")
    public void listConfig(CommandEvent event) {
        var spielersucheRole = configService.get(BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE);
        var serverlogChannel = configService.get(BotConfig.SERVERLOG_KANAL);

        event.reply(
                Replies.standard("config-list"),
                entry("role", spielersucheRole.orElse("no-value-set")),
                entry("serverlog", serverlogChannel.orElse("no-value-set"))
        );
    }
}
