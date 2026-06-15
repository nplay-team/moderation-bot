package de.nplay.moderationbot.moderation;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;

public class MessageReferenceService {

    public Optional<MessageReference> get(long messageId) {
        return Query.query("SELECT * FROM message_references WHERE message_id = ?")
                .single(Call.of().bind(messageId))
                .mapAs(MessageReference.class)
                .first();
    }

    public void create(Message message) {
        Query.query("INSERT INTO message_references VALUES(?, ?, ?) ON CONFLICT DO NOTHING")
                .single(Call.of().bind(message.getIdLong()).bind(message.getChannelIdLong()).bind(message.getContentRaw()))
                .insert();
    }

    public record MessageReference(long messageId, long channelId, String content) {

        public MessageReference {
            if (content.length() > MessageEmbed.VALUE_MAX_LENGTH) {
                // -100 just to be sure because we do some weird formatting
                content = content.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 100) + "...";
            }
        }

        @MappingProvider("")
        public MessageReference(Row row) throws SQLException {
            this(row.getLong("message_id"), row.getLong("channel_id"), row.getString("content"));
        }

        public String jumpUrl(Guild guild) {
            return Matcher.quoteReplacement("%s\n[Link](%s)".formatted(content, format(guild)));
        }

        private String format(Guild guild) {
            return "https://discord.com/channels/%d/%d/%d".formatted(guild.getIdLong(), channelId, messageId);
        }
    }
}
