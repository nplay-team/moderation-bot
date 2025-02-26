package de.nplay.moderationbot.config;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.AutoComplete;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.config.bot.BotConfig;
import de.nplay.moderationbot.config.bot.type.BotConfigTypes;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Objects;

@Interaction
@Permissions(BotPermissions.ADMINISTRATOR)
public class ConfigCommands {

    @Inject
    private EmbedCache embedCache;

    @AutoComplete("config")
    public void onConfigAutocomplete(AutoCompleteEvent event) {
        if (!event.getName().equals("name")) return;

        var botConfigs = BotConfig.getConfigs(event.getJDA());
        botConfigs.removeIf(it -> !it.key().toLowerCase().contains(event.getValue().toLowerCase()));
        event.replyChoices(botConfigs.stream().map(it -> new Command.Choice(it.displayName(), it.key())).toList());
    }

    @SlashCommand(value = "config set", desc = "Setzt eine Konfigurations-Variable", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void setConfig(CommandEvent event, String name, String value) {
        var botConfig = BotConfig.getConfig(name, event.getJDA());

        if (botConfig.isEmpty()) {
            event.reply(embedCache.getEmbed("configNotFound").injectValue("key", name).injectValue("color", EmbedColors.ERROR));
            return;
        }

        if (!botConfig.get().validate(value)) {
            if (!Objects.equals(botConfig.get().type(), BotConfigTypes.STRING())) {
                event.reply(embedCache.getEmbed("configInvalidSnowflake").injectValue("key", botConfig.get().displayName()).injectValue("value", value).injectValue("color", EmbedColors.ERROR));
                return;
            }

            throw new RuntimeException("String is illegally null");
        }

        ConfigService.set(botConfig.get().key(), value);
        event.reply(embedCache.getEmbed("configSet").injectValue("key", botConfig.get().displayName()).injectValue("value", value).injectValue("color", EmbedColors.SUCCESS));
    }

    @SlashCommand(value = "config get", desc = "Gibt den Wert einer Konfigurations-Variable zurück", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void getConfig(CommandEvent event, String name) {
        var botConfig = BotConfig.getConfig(name, event.getJDA());

        if (botConfig.isEmpty()) {
            event.reply(embedCache.getEmbed("configNotFound").injectValue("key", name).injectValue("color", EmbedColors.ERROR));
            return;
        }

        var value = botConfig.get().formattedValue();

        event.reply(embedCache.getEmbed("configGet").injectValue("key", botConfig.get().displayName()).injectValue("value", value.orElse("Nicht gesetzt")).injectValue("color", EmbedColors.DEFAULT));
    }

    @SlashCommand(value = "config list", desc = "Listet alle Konfigurations-Variablen auf", isGuildOnly = true, enabledFor = Permission.ADMINISTRATOR)
    public void listConfig(CommandEvent event) {
        var configs = BotConfig.getConfigs(event.getJDA());

        var embed = embedCache.getEmbed("configList").injectValue("color", EmbedColors.DEFAULT).toEmbedBuilder();

        configs.forEach(config -> embed.addField(config.displayName(), config.formattedValue().orElse("Nicht gesetzt") + "\nTyp: " + config.type().displayName(), false));

        event.jdaEvent().replyEmbeds(embed.build()).queue();
    }

}
