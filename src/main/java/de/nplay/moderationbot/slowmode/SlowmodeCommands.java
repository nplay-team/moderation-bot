package de.nplay.moderationbot.slowmode;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.embeds.EmbedColors;

import java.time.Duration;

@Interaction
public class SlowmodeCommands {

    @Inject
    private EmbedCache embedCache;

    @Command(value = "slowmode info", desc = "Gibt Informationen zu den aktuellen Slowmode-Einstellungen zurück oder setzt diese.")
    public void slowmodeInfoCommand(
            CommandEvent event
    ) {
        var slowmode = SlowmodeService.getSlowmode(event.getGuildChannel());
        if (slowmode.isPresent()) {
            event.reply(
                    embedCache.getEmbed("slowmodeInfo")
                            .injectValue("color", EmbedColors.DEFAULT)
                            .injectValue("duration", Helpers.durationToString(Duration.ofSeconds(slowmode.get().duration()), true))
            );
        } else {
            event.reply(embedCache.getEmbed("slowmodeNotSet").injectValue("color", EmbedColors.DEFAULT));
        }
    }

    @Command(value = "slowmode set", desc = "Gibt Informationen zu den aktuellen Slowmode-Einstellungen zurück oder setzt diese.")
    public void slowmodeSetCommand(
            CommandEvent event,
            @Param("Wie lang soll der Slowmode sein?") Duration duration
    ) {
        SlowmodeService.setSlowmode(event.getGuildChannel(), (int) duration.toSeconds());
        event.reply(
                embedCache.getEmbed("slowmodeSet")
                        .injectValue("color", EmbedColors.SUCCESS)
                        .injectValue("duration", Helpers.durationToString(duration))
        );
    }

    @Command(value = "slowmode remove", desc = "Entfernt den Slowmode für diesen Channel.")
    public void slowmodeRemoveCommand(CommandEvent event) {
        SlowmodeService.removeSlowmode(event.getGuildChannel());
        event.reply(embedCache.getEmbed("slowmodeRemove").injectValue("color", EmbedColors.SUCCESS));
    }

}
