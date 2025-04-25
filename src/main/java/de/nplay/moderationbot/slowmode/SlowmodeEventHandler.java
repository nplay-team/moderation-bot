package de.nplay.moderationbot.slowmode;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.embeds.EmbedColors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class SlowmodeEventHandler extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SlowmodeEventHandler.class);
    private final EmbedCache embedCache;

    public SlowmodeEventHandler(EmbedCache embedCache) {
        this.embedCache = embedCache;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!isValidUser(event.getMember())) return;

        var channel = event.getGuildChannel();
        var message = event.getMessage();
        var author = event.getAuthor();
        var slowmode = SlowmodeService.getSlowmode(channel);

        if (slowmode.isEmpty()) return;

        Optional<Message> lastMessage = event.getGuild()
                .getTextChannelById(channel.getId())
                .getIterableHistory()
                .stream()
                .filter(it -> author.getId().equals(it.getAuthor().getId()))
                .filter(it -> !it.getId().equals(message.getId()))
                .filter(it -> isWithinSlowmode(message.getTimeCreated().toInstant(), it.getTimeCreated().toInstant(), slowmode.get().duration()))
                .findFirst();

        if (lastMessage.isEmpty()) return;

        message.delete().queue();
        notifyUser(
                author,
                channel.getId(),
                slowmode.get().duration(),
                lastMessage.get().getTimeCreated().toInstant().getEpochSecond()
        );
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (event.getChannel().getType() != ChannelType.GUILD_PUBLIC_THREAD) return;

        var thread = event.getChannel().asThreadChannel();
        if (thread.getParentChannel().getType() != ChannelType.FORUM) return;
        if (!isValidUser(thread.getOwner())) return;

        var forumChannel = thread.getParentChannel().asForumChannel();
        var slowmode = SlowmodeService.getSlowmode(forumChannel);

        if (slowmode.isEmpty()) return;

        Optional<ThreadChannel> lastPost = event.getGuild()
                .getThreadChannels()
                .stream()
                .filter(it -> forumChannel.getId().equals(it.getParentChannel().getId()))
                .filter(it -> thread.getOwnerId().equals(it.getOwnerId()))
                .filter(it -> !thread.getId().equals(it.getId()))
                .filter(it -> isWithinSlowmode(thread.getTimeCreated().toInstant(), it.getTimeCreated().toInstant(), slowmode.get().duration()))
                .findFirst();


        if (lastPost.isEmpty()) return;

        thread.delete().queue();
        notifyUser(
                thread.getOwner().getUser(),
                forumChannel.getId(),
                slowmode.get().duration(),
                lastPost.get().getTimeCreated().toInstant().getEpochSecond()
        );
    }

    private boolean isValidUser(Member member) {
        return member != null && !member.getUser().isBot() && !member.hasPermission(Permission.MANAGE_CHANNEL);
    }

    private boolean isWithinSlowmode(Instant current, Instant last, long slowmodeSeconds) {
        return current.toEpochMilli() - last.toEpochMilli() < slowmodeSeconds * 1000L;
    }

    private void notifyUser(User user, String channelId, long duration, long lastMessageTimestamp) {
        user.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(
                    embedCache.getEmbed("slowmodeMessageRemoved")
                            .injectValue("channelId", channelId)
                            .injectValue("duration", Helpers.durationToString(Duration.ofSeconds(duration), true))
                            .injectValue("timestampNextMessage", lastMessageTimestamp + duration)
                            .injectValue("timestampLastMessage", lastMessageTimestamp)
                            .injectValue("color", EmbedColors.ERROR)
                            .toMessageEmbed()
            ).queue();
        });
    }
}

