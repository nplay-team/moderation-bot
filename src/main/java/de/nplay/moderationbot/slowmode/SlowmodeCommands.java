package de.nplay.moderationbot.slowmode;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.embeds.EmbedColors;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.time.Duration;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
public class SlowmodeCommands {

    @Command(value = "slowmode info", desc = "Gibt Informationen zu den aktuellen Slowmode-Einstellungen zurück oder setzt diese.")
    public void slowmodeInfoCommand(
            CommandEvent event,
            @Param(value = "Der Channel, für den die Informationen angezeigt werden sollen.", optional = true) GuildChannel channel
    ) {
        var guildChannel = channel == null ? event.getGuildChannel() : channel;
        var slowmode = SlowmodeService.getSlowmode(guildChannel);
        if (slowmode.isPresent()) {
            event.reply(
                    event.embed("slowmodeInfo")
                            .placeholders(
                                    entry("channelId", guildChannel.getId()),
                                    entry("duration", Helpers.durationToString(Duration.ofSeconds(slowmode.get().duration()), true))
                            )
                            .build()
            );
        } else {
            event.reply(
                    event.embed("slowmodeNotSet").placeholders(entry("channelId", guildChannel.getId())).build()
            );
        }
    }

    @Command(value = "slowmode set", desc = "Setzt den Slowmode für diesen oder einen anderen Kanal.")
    public void slowmodeSetCommand(
            CommandEvent event,
            @Param("Wie lang soll der Slowmode sein?") Duration duration,
            @Param(value = "Der Channel, für den der Slowmode gesetzt werden soll.", optional = true) GuildChannel channel
    ) {
        var guildChannel = channel == null ? event.getGuildChannel() : channel;
        SlowmodeService.setSlowmode(guildChannel, (int) duration.toSeconds());
        event.reply(
                event.embed("slowmodeSet")
                        .placeholders(
                                entry("channelId", guildChannel.getId()),
                                entry("duration", Helpers.durationToString(duration))
                        )
                        .build()
        );
    }

    @Command(value = "slowmode remove", desc = "Entfernt den Slowmode für diesen Channel.")
    public void slowmodeRemoveCommand(
            CommandEvent event,
            @Param(value = "Der Channel, für den der Slowmode entfernt werden soll.", optional = true) GuildChannel channel
    ) {
        var guildChannel = channel == null ? event.getGuildChannel() : channel;
        SlowmodeService.removeSlowmode(guildChannel);
        event.reply(
                event.embed("slowmodeRemove").placeholders(entry("channelId", guildChannel.getId())).build()
        );
    }

}
