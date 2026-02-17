package de.nplay.moderationbot.slowmode;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.lifecycle.Lifecycle;
import de.nplay.moderationbot.auditlog.lifecycle.events.SlowmodeEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.duration.DurationMax;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("slowmode")
@Interaction("slowmode")
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SlowmodeCommands {

    private final SlowmodeService slowmodeService;
    private final Lifecycle lifecycle;

    @Inject
    public SlowmodeCommands(SlowmodeService slowmodeService, Lifecycle lifecycle) {
        this.slowmodeService = slowmodeService;
        this.lifecycle = lifecycle;
    }

    @Command("info")
    public void slowmodeInfoCommand(CommandEvent event, Optional<GuildChannel> channel) {
        var guildChannel = channel.orElse(event.getGuildChannel());
        var slowmode = slowmodeService.get(guildChannel);
        if (slowmode.isEmpty()) {
            event.reply(Replies.error("not-set"), entry("channel", guildChannel));
            return;
        }
        event.reply(
                Replies.standard("info"),
                entry("channel", guildChannel),
                entry("duration", Helpers.formatDuration(slowmode.get().duration()))
        );
    }

    @Command("set")
    public void slowmodeSetCommand(
            CommandEvent event,
            @DurationMax(amount = Integer.MAX_VALUE, unit = ChronoUnit.SECONDS)
            Duration duration,
            Optional<GuildChannel> channel
    ) {
        var guildChannel = channel.orElse(event.getGuildChannel());
        slowmodeService.set(guildChannel, duration);
        lifecycle.publish(new SlowmodeEvent(AuditlogType.SLOWMODE_UPDATE, event.getUser(), guildChannel, duration));
        event.reply(Replies.success("set"), entry("channel", guildChannel), entry("duration", Helpers.formatDuration(duration)));
    }

    @Command("remove")
    public void slowmodeRemoveCommand(CommandEvent event, Optional<GuildChannel> channel) {
        var guildChannel = channel.orElse(event.getGuildChannel());
        slowmodeService.delete(guildChannel);
        lifecycle.publish(new SlowmodeEvent(AuditlogType.SLOWMODE_UPDATE, event.getUser(), guildChannel, null));
        event.reply(Replies.standard("remove"), entry("channel", guildChannel));
    }
}
