package de.nplay.moderationbot.moderation.commands.purge;

import de.nplay.moderationbot.Replies;
import io.github.kaktushose.jdac.annotations.constraints.Max;
import io.github.kaktushose.jdac.annotations.constraints.Min;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.CommandConfig;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Interaction
@CommandConfig(enabledFor = Permission.MESSAGE_MANAGE)
@Permissions(BotPermissions.MODERATION_CREATE)
public class PurgeMessagesCommands {

    private final Serverlog serverlog;

    @Inject
    public PurgeMessagesCommands(Serverlog serverlog) {
        this.serverlog = serverlog;
    }

    @Command("mod purge messages")
    public void purgeMessagesByAmount(CommandEvent event, @Min(1) @Max(100) int amount) {
        var deletedMessages = purgeMessages(event, event.getMessageChannel(), event.getMessageChannel().getLatestMessageId(), amount) - 1;
        replyEvent(event, deletedMessages);
    }

    @Command(value = "Nachrichten bis hier löschen", type = Type.MESSAGE)
    public void purgeMessagesUntilHere(CommandEvent event, Message pivot) {
        var deletedMessages = purgeMessages(event, event.getMessageChannel(), pivot.getId(), null);
        replyEvent(event, deletedMessages);
    }

    private int purgeMessages(CommandEvent event, MessageChannel channel, String pivotMessageId,
                              @Nullable Integer amount) {
        List<String> messageIds = new ArrayList<>();

        messageIds.add(pivotMessageId);
        MessageHistory history;
        if (amount == null) {
            history = MessageHistory.getHistoryAfter(channel, pivotMessageId).complete();
        } else {
            history = MessageHistory.getHistoryBefore(channel, pivotMessageId).limit(amount).complete();
        }
        messageIds.addAll(history.getRetrievedHistory()
                .stream()
                .map(Message::getId)
                .toList()
        );

        channel.purgeMessagesById(messageIds);
        serverlog.onEvent(ModerationEvents.BulkMessageDeletion(channel.getJDA(), event.getGuild(), messageIds.size(), event.getUser()), event);
        return messageIds.size();
    }

    private void replyEvent(CommandEvent event, int amount) {
        var message = event.reply(Replies.success("purge-success"), entry("amount", amount));
        message.delete().queueAfter(5, TimeUnit.SECONDS);
    }

}
