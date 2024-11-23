package de.nplay.moderationbot.moderation;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.chojo.sadu.queries.api.results.writing.insertion.InsertionResult;
import de.nplay.moderationbot.rules.RuleService;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

/**
 * Utility class for managing moderations.
 */
public class ModerationService {

    /**
     * Record representing a moderation.
     *
     * @param id               The unique identifier of the moderation.
     * @param userId           The ID of the user being moderated.
     * @param type             The type of moderation.
     * @param reverted         Whether the moderation has been reverted.
     * @param reason           The reason for the moderation.
     * @param paragraph        The rule paragraph associated with the moderation.
     * @param referenceMessage The reference message associated with the moderation.
     * @param revokeAt         The timestamp when the moderation will be revoked.
     * @param duration         The duration of the moderation.
     * @param issuerId         The ID of the user who issued the moderation.
     * @param created_at       The date when the moderation was created.
     */
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
            Date created_at
    ) {

        /**
         * Mapping method for the {@link de.chojo.sadu.mapper.rowmapper.RowMapper RowMapper}
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
                    Optional.ofNullable(row.getInt("paragraph_id") == 0 ? null : RuleService.getRuleParagraph(row.getInt("paragraph_id"))),
                    Optional.ofNullable(row.getLong("reference_message") == 0 ? null : MessageReferenceService.getMessageReference(row.getLong("reference_message"))),
                    Optional.ofNullable(row.getTimestamp("revokeAt")),
                    Optional.ofNullable(row.getLong("duration") == 0 ? null : row.getLong("duration")),
                    row.getLong("issuer_id"),
                    row.getDate("created_at")
            );
        }

        @Override
        public String toString() {
            return "Moderation{" +
                    "id=" + id +
                    ", userId=" + userId +
                    ", type=" + type +
                    ", reverted=" + reverted +
                    ", reason=" + reason +
                    ", paragraph=" + paragraph +
                    ", referenceMessage=" + referenceMessage +
                    ", revokeAt=" + revokeAt +
                    ", duration=" + duration +
                    ", issuerId=" + issuerId +
                    ", created_at=" + created_at +
                    '}';
        }
    }

    /**
     * Creates a new moderation record in the database.
     *
     * @param act The {@link ModerationAct} to create.
     * @return The {@link InsertionResult} (with keys) of the operation.
     */
    public static InsertionResult createModerationAct(ModerationAct act) {
        return Query.query("INSERT INTO moderations (user_id, type, reverted, issuer_id, reason, paragraph_id, reference_message, revoke_at, duration) VALUES (?, ?::reporttype, ?, ?, ?, ?, ?, ?, ?)")
                .single(Call.of()
                        .bind(act.userId)
                        .bind(act.type)
                        .bind(act.reverted)
                        .bind(act.issuerId)
                        .bind(act.reason.orElse(null))
                        .bind(act.paragraph.map(RuleService.RuleParagraph::id).orElse(null))
                        .bind(act.referenceMessage.map(MessageReferenceService.MessageReference::messageId).orElse(null))
                        .bind(act.revokeAt.orElse(null))
                        .bind(act.duration.orElse(null))
                ).insertAndGetKeys();
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
     * Updates an existing moderation record in the database.
     *
     * @param act The {@link ModerationAct} to update.
     */
    public static void updateModerationAct(ModerationAct act) {
        Query.query("UPDATE moderations SET user_id = ?, type = ?::reporttype, reverted = ?, issuer_id = ?, reason = ?, paragraph_id = ?, reference_message = ?, revoke_at = ?, duration = ? WHERE id = ?")
                .single(Call.of()
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
}
