package de.nplay.moderationbot.slowmode;

import com.github.kaktushose.jda.commands.embeds.Embed;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.permissions.BotPermissionsService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import static com.github.kaktushose.jda.commands.message.i18n.I18n.entry;

public class SlowmodeEventHandler extends ListenerAdapter {

    private final Function<String, Embed> embedFunction;

    public SlowmodeEventHandler(Function<String, Embed> embedFunction) {
        this.embedFunction = embedFunction;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || !event.isFromGuild()) {
            return;
        }

        if (slowModeImmune(event.getMember())) {
            return;
        }

        var channel = event.getGuildChannel();
        var message = event.getMessage();
        var author = event.getAuthor();
        var slowmode = SlowmodeService.getSlowmode(channel);

        if (slowmode.isEmpty()) {
            return;
        }

        Optional<Message> lastMessage = event.getGuild()
                .getTextChannelById(channel.getId())
                .getIterableHistory()
                .stream()
                .filter(it -> author.getId().equals(it.getAuthor().getId()))
                .filter(it -> !it.getId().equals(message.getId()))
                .filter(it -> isWithinSlowmode(message.getTimeCreated().toInstant(), it.getTimeCreated().toInstant(), slowmode.get().duration()))
                .findFirst();

        if (lastMessage.isEmpty()) {
            return;
        }

        message.delete().queue();
        notifyUser(
                author,
                event.getJDA(),
                channel.getId(),
                slowmode.get().duration(),
                lastMessage.get().getTimeCreated().toInstant().getEpochSecond()
        );
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (event.getChannel().getType() != ChannelType.GUILD_PUBLIC_THREAD) {
            return;
        }

        var thread = event.getChannel().asThreadChannel();
        Member owner = event.getGuild().retrieveMemberById(thread.getOwnerId()).complete();
        if (thread.getParentChannel().getType() != ChannelType.FORUM || slowModeImmune(owner)) {
            return;
        }

        var forumChannel = thread.getParentChannel().asForumChannel();
        var slowmode = SlowmodeService.getSlowmode(forumChannel);

        if (slowmode.isEmpty()) {
            return;
        }

        Optional<ThreadChannel> lastPost = event.getGuild()
                .getThreadChannels()
                .stream()
                .filter(it -> forumChannel.getId().equals(it.getParentChannel().getId()))
                .filter(it -> thread.getOwnerId().equals(it.getOwnerId()))
                .filter(it -> !thread.getId().equals(it.getId()))
                .filter(it -> isWithinSlowmode(thread.getTimeCreated().toInstant(), it.getTimeCreated().toInstant(), slowmode.get().duration()))
                .findFirst();


        if (lastPost.isEmpty()) {
            return;
        }

        thread.delete().queue();
        notifyUser(
                owner.getUser(),
                event.getJDA(),
                forumChannel.getId(),
                slowmode.get().duration(),
                lastPost.get().getTimeCreated().toInstant().getEpochSecond()
        );
    }

    private boolean slowModeImmune(Member member) {
        if (member.getUser().isBot()) {
            return true;
        }
        if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
            return true;
        }
        return BotPermissionsService.getUserPermissions(member).hasPermission(BotPermissions.MODERATION_CREATE);
    }

    private boolean isWithinSlowmode(Instant current, Instant last, long slowmodeSeconds) {
        return current.toEpochMilli() - last.toEpochMilli() < slowmodeSeconds * 1000L;
    }

    private void notifyUser(User user, JDA jda, String channelId, long duration, long lastMessageTimestamp) {
        Helpers.sendDM(user, jda, channel ->
                channel.sendMessageEmbeds(embedFunction.apply("slowmodeMessageRemoved").placeholders(
                        entry("channelId", channelId),
                        entry("duration", Helpers.formatDuration(Duration.ofSeconds(duration))),
                        entry("timestampNextMessage", TimeFormat.RELATIVE.format(lastMessageTimestamp + duration)),
                        entry("timestampLastMessage", TimeFormat.RELATIVE.format(lastMessageTimestamp))
                ).build())
        );
    }
}

