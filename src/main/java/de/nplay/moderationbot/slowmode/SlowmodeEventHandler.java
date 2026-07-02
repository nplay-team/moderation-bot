package de.nplay.moderationbot.slowmode;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.Replies.RelativeTime;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.permissions.PermissionsService;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.components.container.SeparatedContainer;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.ISlowmodeChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("slowmode")
public class SlowmodeEventHandler extends ListenerAdapter {

    private final MessageResolver resolver;
    private final SlowmodeService slowmodeService;
    private final PermissionsService permissionsService;

    public SlowmodeEventHandler(
            MessageResolver resolver,
            SlowmodeService slowmodeService,
            PermissionsService permissionsService
    ) {
        this.resolver = resolver;
        this.slowmodeService = slowmodeService;
        this.permissionsService = permissionsService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMember() == null || slowModeImmune(event.getMember())) {
            return;
        }

        var channel = event.getGuildChannel();

        if (isDiscordHandled(channel)) return;

        var slowmode = slowmodeService.get(channel);

        if (slowmode.isEmpty()) {
            return;
        }

        var message = event.getMessage();
        var author = event.getAuthor();

        Optional<Message> lastMessage = MessageHistory
                .getHistoryAfter(channel, Long.toUnsignedString(TimeUtil.getDiscordTimestamp(System.currentTimeMillis() - slowmode.get().duration().toMillis())))
                .complete()
                .getRetrievedHistory()
                .stream()
                .filter(it -> author.getId().equals(it.getAuthor().getId()))
                .filter(it -> !it.getId().equals(message.getId()))
                .filter(it -> isWithinSlowmode(message.getTimeCreated().toInstant(), it.getTimeCreated().toInstant(), slowmode.get()))
                .findFirst();

        if (lastMessage.isEmpty()) {
            return;
        }

        message.delete().queue();
        notifyUser(
                author,
                event.getJDA(),
                channel,
                slowmode.get().duration(),
                lastMessage.get().getTimeCreated()
        );
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (event.getChannel().getType() != ChannelType.GUILD_PUBLIC_THREAD) {
            return;
        }

        if (isDiscordHandled(event.getChannel().asGuildChannel())) return;

        var thread = event.getChannel().asThreadChannel();
        Member owner = event.getGuild().retrieveMemberById(thread.getOwnerId()).complete();
        if (thread.getParentChannel().getType() != ChannelType.FORUM || slowModeImmune(owner)) {
            return;
        }

        var forumChannel = thread.getParentChannel().asForumChannel();
        var slowmode = slowmodeService.get(forumChannel);

        if (slowmode.isEmpty()) {
            return;
        }

        Optional<ThreadChannel> lastPost = event.getGuild()
                .getThreadChannels()
                .stream()
                .filter(it -> forumChannel.getId().equals(it.getParentChannel().getId()))
                .filter(it -> thread.getOwnerId().equals(it.getOwnerId()))
                .filter(it -> !thread.getId().equals(it.getId()))
                .filter(it -> isWithinSlowmode(thread.getTimeCreated().toInstant(), it.getTimeCreated().toInstant(), slowmode.get()))
                .findFirst();

        if (lastPost.isEmpty()) {
            return;
        }

        thread.delete().queue();
        notifyUser(
                owner.getUser(),
                event.getJDA(),
                forumChannel,
                slowmode.get().duration(),
                lastPost.get().getTimeCreated()
        );
    }

    private boolean slowModeImmune(Member member) {
        if (member.getUser().isBot()) {
            return true;
        }
        if (member.hasPermission(Permission.BYPASS_SLOWMODE)) {
            return true;
        }
        return permissionsService.getCombined(member).hasPermission(BotPermissions.MODERATION_CREATE);
    }

    private boolean isWithinSlowmode(Instant current, Instant last, SlowmodeService.Slowmode slowmode) {
        if(last.isBefore(slowmode.createdAt().toInstant())) return false;
        return current.toEpochMilli() - last.toEpochMilli() < slowmode.duration().toMillis();
    }

    private boolean isDiscordHandled(GuildChannel channel) {
        return channel instanceof ISlowmodeChannel slowmodeChannel && slowmodeChannel.getSlowmode() > 0;
    }

    private void notifyUser(User user, JDA jda, Channel channel, Duration duration, OffsetDateTime last) {
        SeparatedContainer container = new SeparatedContainer(
                resolver,
                Locale.GERMAN,
                TextDisplay.of("removed"),
                Separator.createDivider(Separator.Spacing.SMALL)

        ).entries(
                entry("channel", channel),
                entry("duration", Helpers.formatDuration(duration))
        ).withAccentColor(Replies.STANDARD);

        container.add(TextDisplay.of("removed.next"), entry("next", RelativeTime.of(last.plus(duration))));
        container.add(TextDisplay.of("removed.last"), entry("last", RelativeTime.of(last)));
        Helpers.sendDM(user, jda, it -> it.sendMessageComponents(container).useComponentsV2());
    }
}

