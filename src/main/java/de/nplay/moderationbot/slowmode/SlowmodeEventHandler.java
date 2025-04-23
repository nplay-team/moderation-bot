package de.nplay.moderationbot.slowmode;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SlowmodeEventHandler extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SlowmodeEventHandler.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot()) return;
        if (event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) return;

        var channel = event.getGuildChannel();

        var slowmode = SlowmodeService.getSlowmode(channel);

        if (slowmode.isPresent()) {
            Optional<Message> lastMessage = event.getGuild().getTextChannelById(channel.getId())
                    .getIterableHistory()
                    .stream()
                    .filter(message -> message.getAuthor().getId().equals(event.getAuthor().getId()))
                    .filter(message -> !message.getId().equals(event.getMessageId()))
                    .findFirst();

            if (lastMessage.isPresent()) {
                if (event.getMessage().getTimeCreated().toInstant().toEpochMilli() - lastMessage.get().getTimeCreated().toInstant().toEpochMilli() < slowmode.get().duration() * 1000L) {
                    event.getMessage().delete().queue();
                    log.info("Deleted message from {} in {} because of slowmode", event.getAuthor().getAsTag(), channel.getName());
                }
            }
        }
    }
}
