package de.nplay.moderationbot.slowmode;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.duration.DurationMax;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.time.Duration;
import java.util.Optional;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction("slowmode")
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SlowmodeCommands {

    @Command("info")
    public void slowmodeInfoCommand(CommandEvent event, Optional<GuildChannel> channel) {
        var guildChannel = channel.orElse(event.getGuildChannel());
        var slowmode = SlowmodeService.getSlowmode(guildChannel);
        if (slowmode.isEmpty()) {
            event.with().embeds("slowmodeNotSet", entry("channelId", guildChannel.getId())).reply();
            return;
        }
        event.with().embeds("slowmodeInfo",
                entry("channelId", guildChannel.getId()),
                entry("duration", Helpers.durationToString(Duration.ofSeconds(slowmode.get().duration()), true))
        ).reply();
    }

    @Command("set")
    public void slowmodeSetCommand(CommandEvent event, @DurationMax(Integer.MAX_VALUE) Duration duration, Optional<GuildChannel> channel) {
        var guildChannel = channel.orElse(event.getGuildChannel());
        SlowmodeService.setSlowmode(guildChannel, (int) duration.toSeconds());
        event.with().embeds("slowmodeSet",
                entry("channelId", guildChannel.getId()),
                entry("duration", Helpers.durationToString(duration))
        ).reply();
    }

    @Command("remove")
    public void slowmodeRemoveCommand(CommandEvent event, Optional<GuildChannel> channel) {
        var guildChannel = channel.orElse(event.getGuildChannel());
        SlowmodeService.removeSlowmode(guildChannel);
        event.with().embeds("slowmodeRemove", entry("channelId", guildChannel.getId())).reply();
    }
}
