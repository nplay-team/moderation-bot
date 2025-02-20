package de.nplay.moderationbot.moderation.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class BulkDeleteCommands {

    @Inject
    private EmbedCache embedCache;

    @Inject
    private Serverlog serverlog;

    @SlashCommand(value = "moderation purge messages", desc = "Löscht eine bestimmte Anzahl an Nachrichten gleichzeitig", isGuildOnly = true, enabledFor = Permission.MESSAGE_MANAGE)
    public void purgeMessagesByAmount(CommandEvent event, @Param("Anzahl der Nachrichten die gelöscht werden sollen") @Min(1) @Max(100) int amount) {
        var deletedMessages = purgeMessages(event, event.getMessageChannel(), event.getMessageChannel().getLatestMessageId(), amount);
        replyEvent(event, deletedMessages);
    }

    @ContextCommand(value = "Nachrichten bis hier löschen", enabledFor = Permission.MESSAGE_MANAGE, isGuildOnly = true, type = Command.Type.MESSAGE)
    public void purgeMessagesUntilHere(CommandEvent event, Message pivot) {
        var deletedMessages = purgeMessages(event, event.getMessageChannel(), pivot.getId(), null);
        replyEvent(event, deletedMessages);
    }

    private int purgeMessages(CommandEvent event, MessageChannel channel, String pivotMessageId, @Nullable Integer amount) {
        List<String> messageIds;

        if (amount == null) {
            messageIds = new ArrayList<>(MessageHistory.getHistoryAfter(channel, pivotMessageId)
                    .complete()
                    .getRetrievedHistory()
                    .stream()
                    .map(Message::getId)
                    .toList()
            );
        } else {
            messageIds = new ArrayList<>(MessageHistory.getHistoryBefore(channel, pivotMessageId)
                    .limit(amount - 1)
                    .complete()
                    .getRetrievedHistory()
                    .stream()
                    .map(Message::getId)
                    .toList()
            );

            messageIds.add(pivotMessageId);
        }

        channel.purgeMessagesById(messageIds);
        serverlog.onEvent(ModerationEvents.BulkMessageDeletion(channel.getJDA(), event.getGuild(), messageIds.size(), event.getUser()));
        return messageIds.size();
    }

    private void replyEvent(CommandEvent event, int amount) {
        var message = event.reply(EmbedHelpers.getBulkMessageDeletionSuccessfulEmbed(embedCache, amount));
        message.delete().queueAfter(5, TimeUnit.SECONDS);
    }

}
