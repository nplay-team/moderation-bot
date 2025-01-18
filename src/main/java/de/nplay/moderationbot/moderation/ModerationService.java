package de.nplay.moderationbot.moderation;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapper;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Bootstrapper;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationActBuilder.ModerationActCreateAction;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for managing moderation acts.
 */
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    public static ModerationAct createModerationAct(ModerationActCreateAction action) {
        if (action.messageReference() != null) {
            MessageReferenceService.createMessageReference(action.messageReference());
        }

        var id = Query.query("""
                INSERT INTO moderations
                (user_id, type, reverted, issuer_id, reason, paragraph_id, reference_message, revoke_at, duration, created_at)
                VALUES (?, ?::reporttype, ?, ?, ?, ?, ?, ?, ?, ?)
                """
        ).single(Call.of()
                .bind(action.targetId())
                .bind(action.type())
                .bind(false)
                .bind(action.issuerId())
                .bind(action.reason())
                .bind(action.paragraphId())
                .bind(action.messageReference() == null ? null : action.messageReference().messageId())
                .bind(action.revokeAt().orElse(null))
                .bind(action.duration())
                .bind(new Timestamp(System.currentTimeMillis()))
        ).insertAndGetKeys().keys().getFirst();

        return getModerationAct(id);
    }

    /**
     * Retrieves a moderation by its ID.
     *
     * @param moderationId The ID of the moderation to retrieve.
     * @return The {@link ModerationAct} record.
     */
    public static ModerationAct getModerationAct(long moderationId) {
        return Query.query("SELECT * FROM moderations WHERE id = ?")
                .single(Call.of().bind(moderationId))
                .mapAs(ModerationAct.class)
                .first().orElseThrow();
    }

    /**
     * Retrieves all moderation's, which need to be reverted.
     *
     * @return a list of {@link ModerationAct} records.
     */
    public static List<ModerationAct> getModerationActsToRevert() {
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

    public static void revertModerationAct(ModerationAct act) {
        act.revert();

        Query.query("UPDATE moderations SET reverted = true WHERE id = ?")
                .single(Call.of().bind(act.id))
                .update();

        log.info("Reverted moderation act with id {}", act.id);
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
            Timestamp created_at,
            Optional<Integer> delDays
    ) {

        private static final Logger logger = LoggerFactory.getLogger(ModerationAct.class);

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

        public void revert() {
            if (type == ModerationActType.BAN || type == ModerationActType.TEMP_BAN) {
                var guild = Bootstrapper.bot.getGuild();

                try {
                    guild.unban(UserSnowflake.fromId(String.valueOf(userId))).queue();
                } catch (Exception e) {
                    logger.error("Failed to unban user {}", userId, e);
                }
            }

            if (type == ModerationActType.TIMEOUT) {
                Bootstrapper.bot.getGuild().retrieveMemberById(userId).flatMap(Member::removeTimeout).queue();
            }

            if (type == ModerationActType.TIMEOUT || type == ModerationActType.WARN) {
                var user = Bootstrapper.bot.getJda().getUserById(userId);
                var issuer = Bootstrapper.bot.getJda().getUserById(issuerId);
                var issuerUsername = issuer != null ? issuer.getName() : "System";
                var embedCache = Bootstrapper.bot.getEmbedCache();

                if (user != null) {
                    user.openPrivateChannel().flatMap(it -> it.sendMessageEmbeds(
                                    embedCache
                                            .getEmbed(type == ModerationActType.TIMEOUT ? "timeoutReverted" : "warnReverted")
                                            .injectValue("date", created_at.getTime() / 1000)
                                            .injectValue("id", id)
                                            .injectValue("issuerId", issuerId)
                                            .injectValue("issuerUsername", issuerUsername)
                                            .injectValue("color", EmbedColors.SUCCESS)
                                            .toMessageEmbed()
                            )
                    ).queue();
                }
            }

            logger.info("Reverting moderation action: {}", this);
        }
    }
}
