package de.nplay.moderationbot.trap;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("trap")
@Interaction("trap")
public class TrapChannelCommands {

    private final TrapChannelService service;

    @Inject
    public TrapChannelCommands(TrapChannelService service) {
        this.service = service;
    }

    @Command("place")
    public void place(CommandEvent event, TextChannel channel) {
        if (service.get(channel).isPresent()) {
            event.reply(Replies.error("already-trap-channel"), entry("channel", channel));
            return;
        }

        service.set(channel);
        event.reply(Replies.success("placed"), entry("channel", channel));
    }

    @Command("info")
    public void info(CommandEvent event, TextChannel channel) {
        event.reply(
                Replies.success("info"),
                entry("channel", channel), entry("placed", service.get(channel).isPresent())
        );
    }

    @Command("remove")
    public void remove(CommandEvent event, TextChannel channel) {
        if (service.get(channel).isEmpty()) {
            event.reply(Replies.error("not-trap-channel"), entry("channel", channel));
            return;
        }

        service.remove(channel);
        event.reply(Replies.success("removed"), entry("channel", channel));
    }
}
