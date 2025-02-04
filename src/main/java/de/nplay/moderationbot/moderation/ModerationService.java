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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
                (user_id, type, issuer_id, reason, paragraph_id, reference_message, revoke_at, duration, created_at, reverted)
                VALUES (?, ?::reporttype, ?, ?, ?, ?, ?, ?, ?, false)
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
                .bind(new Timestamp(System.currentTimeMillis()))
        ).insertAndGetKeys().keys().getFirst();

        return getModerationAct(id).orElseThrow();
    }

    /**
     * Retrieves all moderation's, which need to be reverted.
     *
     * @return a list of {@link ModerationAct} records.
     */
    public static Collection<ModerationAct> getModerationActsToRevert() {
        return Query.query("SELECT * FROM moderations WHERE reverted = false AND revoke_at < ? ORDER BY created_at DESC")
                .single(Call.of().bind(new Timestamp(System.currentTimeMillis())))
                .mapAs(ModerationAct.class)
                .all();
    }

    public static List<ModerationAct> getModerationActs(UserSnowflake user) {
        return getModerationActs(user, null, null, false);
    }

    public static List<ModerationAct> getModerationActs(UserSnowflake user, Integer limit, Integer offset) {
        return getModerationActs(user, limit, offset, false);
    }

    public static List<ModerationAct> getModerationActs(UserSnowflake user, @Nullable Integer limit, @Nullable Integer offset, Boolean includeReverted) {
        return Query.query("SELECT * FROM moderations WHERE user_id = ? AND reverted = ? ORDER BY created_at DESC LIMIT ? OFFSET ?")
                .single(Call.of()
                        .bind(user.getIdLong())
                        .bind(includeReverted)
                        .bind(limit == null ? 25 : limit)
                        .bind(offset == null ? 0 : offset)
                ).mapAs(ModerationAct.class)
                .all();
    }

    public static Integer getModerationActCount(UserSnowflake user) {
        return getModerationActCount(user, false);
    }

    public static Integer getModerationActCount(UserSnowflake user, Boolean includeReverted) {
        return Query.query("SELECT COUNT(*) FROM moderations WHERE user_id = ? AND reverted = ?")
                .single(Call.of()
                        .bind(user.getIdLong())
                        .bind(includeReverted)
                ).mapAs(Integer.class)
                .first().orElse(0);
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
                .bind(act.reason)
                .bind(act.paragraph != null ? act.paragraph.id() : null)
                .bind(act.referenceMessage != null ? act.referenceMessage.messageId() : null)
                .bind(act.revokeAt)
                .bind(act.duration)
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
            Long id,
            Long userId,
            ModerationActType type,
            Boolean reverted,
            @Nullable Long revertedBy,
            @Nullable Timestamp revertedAt,
            @Nullable String revertingReason,
            @Nullable String reason,
            @Nullable RuleService.RuleParagraph paragraph,
            @Nullable MessageReferenceService.MessageReference referenceMessage,
            @Nullable Timestamp revokeAt,
            @Nullable Long duration,
            Long issuerId,
            Timestamp createdAt,
            @Nullable Integer delDays
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
                    row.getLong("reverted_by") == 0 ? null : row.getLong("reverted_by"),
                    row.getTimestamp("reverted_at"),
                    row.getString("revert_reason"),
                    row.getString("reason"),
                    RuleService.getRuleParagraph(row.getInt("paragraph_id")).orElse(null),
                    MessageReferenceService.getMessageReference(row.getLong("reference_message")).orElse(null),
                    row.getTimestamp("revoke_at"),
                    row.getLong("duration") == 0 ? null : row.getLong("duration"),
                    row.getLong("issuer_id"),
                    row.getTimestamp("created_at"),
                    null
            );
        }

        public void revert(Guild guild, EmbedCache embedCache, User revertedBy, @Nullable String reason) {
            log.info("Reverting moderation action: {}", this);

            Query.query("UPDATE moderations SET reverted = true, reverted_by = ?, reverted_at = ?, revert_reason = ? WHERE id = ?")
                    .single(Call.of()
                            .bind(revertedBy.getIdLong())
                            .bind(new Timestamp(System.currentTimeMillis()))
                            .bind(reason)
                            .bind(id))
                    .update();

            switch (type) {
                case BAN, TEMP_BAN -> guild.unban(UserSnowflake.fromId(String.valueOf(userId))).queue(_ -> {}, UNKNOWN_USER_HANDLER);
                case TIMEOUT -> {
                    guild.retrieveMemberById(userId).flatMap(Member::removeTimeout).queue(_ -> {}, UNKNOWN_USER_HANDLER);
                    sendRevertMessageToUser(guild, embedCache, revertedBy, reason);
                }
                case WARN -> sendRevertMessageToUser(guild, embedCache, revertedBy, reason);
            }
        }

        private void sendRevertMessageToUser(Guild guild, EmbedCache embedCache, User revertedBy, @Nullable String revertingReason) {
            var embed = embedCache
                    .getEmbed(type == ModerationActType.TIMEOUT ? "timeoutReverted" : "warnReverted")
                    .injectValue("date", createdAt.getTime() / 1000)
                    .injectValue("id", id)
                    .injectValue("reason", Objects.requireNonNullElse(revertingReason, "?DEL?"))
                    .injectValue("revertedById", revertedBy.getIdLong())
                    .injectValue("revertedByUsername", revertedBy.getName())
                    .injectValue("color", EmbedColors.SUCCESS).toEmbedBuilder();

            embed.getFields().removeIf(it -> "?DEL?".equals(it.getValue()));

            guild.retrieveMemberById(userId).flatMap(it -> it.getUser().openPrivateChannel())
                    .flatMap(channel -> channel.sendMessageEmbeds(embed.build()))
                    .queue(_ -> {
                    }, UNKNOWN_USER_HANDLER);
        }
    }
}
