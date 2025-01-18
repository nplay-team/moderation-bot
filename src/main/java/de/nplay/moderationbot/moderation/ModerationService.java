package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapper;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationActBuilder.ModerationActCreateData;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;

import static de.nplay.moderationbot.Helpers.UNKNOWN_USER_HANDLER;

/**
 * Utility class for managing moderation acts.
 */
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    /**
     * Retrieves a moderation by its ID.
     *
     * @param moderationId The ID of the moderation to retrieve.
     * @return The {@link ModerationAct} record.
     */
    public static Optional<ModerationAct> getModerationAct(long moderationId) {
        return Query.query("SELECT * FROM moderations WHERE id = ?")
                .single(Call.of().bind(moderationId))
                .mapAs(ModerationAct.class)
                .first();
    }

    public static ModerationAct createModerationAct(ModerationActCreateData data) {
        if (data.messageReference() != null) {
            MessageReferenceService.createMessageReference(data.messageReference());
        }

        var id = Query.query("""
                INSERT INTO moderations
                (user_id, type, issuer_id, reason, paragraph_id, reference_message, revoke_at, duration)
                VALUES (?, ?::reporttype, ?, ?, ?, ?, ?, ?)
                """
        ).single(Call.of()
                .bind(data.targetId())
                .bind(data.type())
                .bind(data.issuerId())
                .bind(data.reason())
                .bind(data.paragraphId())
                .bind(data.messageReferenceId().orElse(null))
                .bind(data.revokeAt().orElse(null))
                .bind(data.duration())
        ).insertAndGetKeys().keys().getFirst();

        return getModerationAct(id).orElseThrow();
    }

    /**
     * Retrieves all moderation's, which need to be reverted.
     *
     * @return a list of {@link ModerationAct} records.
     */
    public static Collection<ModerationAct> getModerationActsToRevert() {
        return Query.query("SELECT * FROM moderations WHERE reverted = false AND revoke_at < ?")
                .single(Call.of().bind(new Timestamp(System.currentTimeMillis())))
                .mapAs(ModerationAct.class)
                .all();
    }

    /**
     * Updates an existing moderation record in the database.
     *
     * @param act The {@link ModerationAct} to update.
     */
    public static void updateModerationAct(ModerationAct act) {
        Query.query("""
                UPDATE moderations SET user_id = ?, type = ?::reporttype, reverted = ?, issuer_id = ?, reason = ?,
                paragraph_id = ?, reference_message = ?, revoke_at = ?, duration = ?
                WHERE id = ?"""
        ).single(Call.of()
                .bind(act.userId)
                .bind(act.type)
                .bind(act.issuerId)
                .bind(act.reverted)
                .bind(act.reason.orElse(null))
                .bind(act.paragraph.map(RuleService.RuleParagraph::id).orElse(null))
                .bind(act.referenceMessage.map(MessageReferenceService.MessageReference::messageId).orElse(null))
                .bind(act.revokeAt.orElse(null))
                .bind(act.duration.orElse(null))
                .bind(act.id)
        ).update();
    }

    /**
     * Deletes a moderation record from the database.
     *
     * @param moderationId The ID of the moderation to delete.
     */
    public static void deleteModerationAct(long moderationId) {
        Query.query("DELETE FROM moderations WHERE id = ?")
                .single(Call.of().bind(moderationId))
                .delete();
    }


    public record ModerationAct(
            long id,
            long userId,
            ModerationActType type,
            Boolean reverted,
            Optional<String> reason,
            Optional<RuleService.RuleParagraph> paragraph,
            Optional<MessageReferenceService.MessageReference> referenceMessage,
            Optional<Timestamp> revokeAt,
            Optional<Long> duration,
            long issuerId,
            Timestamp createdAt,
            Optional<Integer> delDays
    ) {
        /**
         * Mapping method for the {@link RowMapper RowMapper}
         *
         * @return a {@link RowMapping} of this record
         */
        @MappingProvider("")
        public static RowMapping<ModerationAct> map() {
            return row -> new ModerationAct(
                    row.getLong("id"),
                    row.getLong("user_id"),
                    ModerationActType.valueOf(row.getString("type")),
                    row.getBoolean("reverted"),
                    Optional.ofNullable(row.getString("reason")),
                    RuleService.getRuleParagraph(row.getInt("paragraph_id")),
                    MessageReferenceService.getMessageReference(row.getLong("reference_message")),
                    Optional.ofNullable(row.getTimestamp("revoke_at")),
                    Optional.ofNullable(row.getLong("duration") == 0 ? null : row.getLong("duration")),
                    row.getLong("issuer_id"),
                    row.getTimestamp("created_at"),
                    Optional.empty()
            );
        }

        public void revert(Guild guild, EmbedCache embedCache) {
            log.info("Reverting moderation action: {}", this);
            Query.query("UPDATE moderations SET reverted = true WHERE id = ?")
                    .single(Call.of().bind(id))
                    .update();
            switch (type) {
                case BAN, TEMP_BAN -> guild.unban(UserSnowflake.fromId(String.valueOf(userId))).queue(_ -> {}, UNKNOWN_USER_HANDLER);
                case TIMEOUT -> {
                    guild.retrieveMemberById(userId).flatMap(Member::removeTimeout).queue(_ -> {}, UNKNOWN_USER_HANDLER);
                    sendMessage(guild, embedCache);
                }
                case WARN -> sendMessage(guild, embedCache);
            }
        }

        private void sendMessage(Guild guild, EmbedCache embedCache) {
            guild.retrieveMemberById(userId).flatMap(it -> it.getUser().openPrivateChannel())
                    .flatMap(channel -> channel.sendMessageEmbeds(embedCache
                            .getEmbed(type == ModerationActType.TIMEOUT ? "timeoutReverted" : "warnReverted")
                            .injectValue("date", createdAt.getTime() / 1000)
                            .injectValue("id", id)
                            .injectValue("issuerId", issuerId)
                            .injectValue("issuerUsername", guild.retrieveMemberById(issuerId).complete().getEffectiveName())
                            .injectValue("color", EmbedColors.SUCCESS)
                            .toMessageEmbed())
                    ).queue(_ -> {}, UNKNOWN_USER_HANDLER);
        }
    }
}
