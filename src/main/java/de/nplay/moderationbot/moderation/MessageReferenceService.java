package de.nplay.moderationbot.moderation;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Optional;
import java.util.regex.Matcher;

public class MessageReferenceService {

    public static Optional<MessageReference> getMessageReference(long messageId) {
        return Query.query("SELECT * FROM message_references WHERE message_id = ?")
                .single(Call.of().bind(messageId))
                .mapAs(MessageReference.class)
                .first();
    }

    public static void createMessageReference(Message message) {
        Query.query("INSERT INTO message_references VALUES(?, ?, ?) ON CONFLICT DO NOTHING")
                .single(Call.of().bind(message.getIdLong()).bind(message.getChannelIdLong()).bind(message.getContentRaw()))
                .insert();
    }

    public static void deleteMessageReference(long messageId) {
        Query.query("DELETE FROM message_references where message_id = ?")
                .single(Call.of().bind(messageId))
                .delete();
    }

    public record MessageReference(long messageId, long channelId, String content) {

        public MessageReference {
            if (content.length() > MessageEmbed.VALUE_MAX_LENGTH) {
                content = content.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 3) + "...";
            }
        }

        @MappingProvider("")
        public static RowMapping<MessageReference> map() {
            return row -> new MessageReference(
                    row.getLong("message_id"),
                    row.getLong("channel_id"),
                    row.getString("content")
            );
        }

        public String jumpUrl(Guild guild) {
            return Matcher.quoteReplacement("%s\n[Link](%s)".formatted(content, format(guild)));
        }

        private String format(Guild guild) {
            return "https://discord.com/channels/%d/%d/%d".formatted(guild.getIdLong(), channelId, messageId);
        }
    }
}
