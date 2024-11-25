package de.nplay.moderationbot.moderation;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Utility class for managing message references
 */
public class MessageReferenceService {

    /**
     * Gets a {@link MessageReference} based on the message id
     *
     * @param messageId the id of the message
     * @return an Optional holding the {@link MessageReference}
     */
    public static Optional<MessageReference> getMessageReference(long messageId) {
        return Query.query("SELECT * FROM message_references WHERE message_id = ?")
                .single(Call.of().bind(messageId))
                .mapAs(MessageReference.class)
                .first();
    }

    /**
     * Creates a new message reference
     *
     * @param messageId the message id
     * @param channelId the channel id the message was sent in
     */
    public static void createMessageReference(long messageId, long channelId) {
        createMessageReference(messageId, channelId, null);
    }

    /**
     * Creates a new message reference
     *
     * @param messageId the message id
     * @param channelId the channel id the message was sent in
     * @param content   Optional holding the content of the message
     */
    public static void createMessageReference(long messageId, long channelId, @Nullable String content) {
        Query.query("INSERT INTO message_references VALUES(?, ?, ?)")
                .single(Call.of().bind(messageId).bind(channelId).bind(content))
                .insert();
    }

    /**
     * Deletes a message reference from the database
     *
     * @param messageId the id of the message
     */
    public static void deleteMessageReference(long messageId) {
        Query.query("DELETE FROM message_references where message_id = ?")
                .single(Call.of().bind(messageId))
                .delete();
    }

    /**
     * Mapping of a message reference
     *
     * @param messageId the message id
     * @param channelId the channel id the message was sent in
     * @param content   Optional holding the content of the message
     */
    public record MessageReference(long messageId, long channelId, Optional<String> content) {

        /**
         * Mapping method for the {@link de.chojo.sadu.mapper.rowmapper.RowMapper RowMapper}
         *
         * @return a {@link RowMapping} of this record
         */
        @MappingProvider("")
        public static RowMapping<MessageReference> map() {
            return row -> new MessageReference(
                    row.getLong("message_id"),
                    row.getLong("channel_id"),
                    Optional.ofNullable(row.getString("content"))
            );
        }

        /**
         * Gets the jump url to this message reference
         *
         * @param guild the {@link Guild} instance the bot is running on
         * @return the jump url to this message reference
         */
        public String jumpUrl(Guild guild) {
            return String.format("https://discord.com/channels/%d/%d/%d", guild.getIdLong(), channelId, messageId);
        }

    }
}
