package de.nplay.moderationbot.trap;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.function.Consumer;

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
    public void place(CommandEvent event) {
        withTextChannel(event, channel -> {
            if (service.get(channel).isPresent()) {
                event.with().ephemeral(true)
                        .reply(Replies.error("already-trap-channel"), entry("channel", channel));
                return;
            }

            service.set(channel);
            event.with().ephemeral(true)
                    .reply(Replies.success("placed"), entry("channel", channel));
        });
    }

    @Command("info")
    public void info(CommandEvent event) {
        withTextChannel(event, channel -> {
            var placed = Boolean.toString(service.get(channel).isPresent());
            event.with().ephemeral(true)
                .reply(Replies.success("info"), entry("channel", channel), entry("placed", placed));
        });
    }

    @Command("remove")
    public void remove(CommandEvent event) {
        withTextChannel(event, channel -> {
            if (service.get(channel).isEmpty()) {
                event.with().ephemeral(true)
                        .reply(Replies.error("not-trap-channel"), entry("channel", channel));
                return;
            }

            service.remove(channel);
            event.with().ephemeral(true)
                    .reply(Replies.success("removed"), entry("channel", channel));
        });
    }

    private void withTextChannel(CommandEvent event, Consumer<TextChannel> channel) {
        if (event.getGuildChannel() instanceof TextChannel textChannel) {
            channel.accept(textChannel);
            return;
        }
        event.with().ephemeral(true)
                .reply(Replies.error("not-text-channel"), entry("channel", event.getGuildChannel()));
    }

}
