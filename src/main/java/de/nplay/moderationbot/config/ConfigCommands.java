package de.nplay.moderationbot.config;

import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.config.bot.BotConfig;
import net.dv8tion.jda.api.Permission;

import java.util.stream.Collectors;

@Interaction
public class ConfigCommands {

    @SlashCommand(value = "config set", desc = "Setzt eine Konfigurations-Variable", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void setConfig(CommandEvent event, java.lang.String name, java.lang.String value) {
        var botConfig = BotConfig.getConfig(name, event.getJDA());

        if (botConfig.isEmpty()) {
            event.reply("Konfigurations-Variable nicht gefunden");
            return;
        }

        if (!botConfig.get().validate(value)) {
            event.reply("Ungültiger Wert");
            return;
        }

        ConfigService.set(botConfig.get().key(), value);
        event.reply("Konfigurations-Variable gesetzt");
    }

    @SlashCommand(value = "config get", desc = "Gibt den Wert einer Konfigurations-Variable zurück", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void getConfig(CommandEvent event, java.lang.String name) {
        var botConfig = BotConfig.getConfig(name, event.getJDA());

        if (botConfig.isEmpty()) {
            event.reply("Konfigurations-Variable nicht gefunden");
            return;
        }

        var value = botConfig.get().value();

        if (value.isEmpty()) {
            event.reply("Konfigurations-Variable nicht gesetzt");
            return;
        }

        event.reply(botConfig.get().type().formatter().apply(value.get()));
    }

    @SlashCommand(value = "config list", desc = "Listet alle Konfigurations-Variablen auf", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void listConfig(CommandEvent event) {
        var configs = BotConfig.getConfigs(event.getJDA());

        if (configs.isEmpty()) {
            event.reply("Keine Konfigurations-Variablen gefunden");
            return;
        }

        var response = configs.stream()
                .map(config -> config.key() + " (" + config.type().displayName() + ") " + ": " + (config.value().isPresent() ? config.type().formatter().apply(config.value().get()) : "Nicht gesetzt"))
                .collect(Collectors.joining("\n"));

        event.reply(response);
    }

}
