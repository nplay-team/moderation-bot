package de.nplay.moderationbot.moderation;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.chojo.sadu.queries.api.results.writing.insertion.InsertionResult;
import de.nplay.moderationbot.rules.RuleService;

import javax.annotation.Nullable;
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
     * @param revoke_at        The timestamp when the moderation will be revoked.
     * @param duration         The duration of the moderation.
     * @param issuerId         The ID of the user who issued the moderation.
     * @param created_at       The date when the moderation was created.
     */
    public record Moderation(
            long id,
            long userId,
            ModerationType type,
            Optional<Boolean> reverted,
            Optional<String> reason,
            Optional<RuleService.RuleParagraph> paragraph,
            Optional<MessageReferenceService.MessageReference> referenceMessage,
            Optional<Timestamp> revoke_at,
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
        public static RowMapping<Moderation> map() {
            return row -> new Moderation(
                    row.getLong("id"),
                    row.getLong("user_id"),
                    ModerationType.valueOf(row.getString("type")),
                    Optional.ofNullable(row.getBoolean("reverted") ? row.getBoolean("reverted") : null),
                    Optional.ofNullable(row.getString("reason")),
                    Optional.ofNullable(row.getInt("paragraph_id") == 0 ? null : RuleService.getRuleParagraph(row.getInt("paragraph_id"))),
                    Optional.ofNullable(row.getLong("reference_message") == 0 ? null : MessageReferenceService.getMessageReference(row.getLong("reference_message"))),
                    Optional.ofNullable(row.getTimestamp("revoke_at")),
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
                    ", revoke_at=" + revoke_at +
                    ", duration=" + duration +
                    ", issuerId=" + issuerId +
                    ", created_at=" + created_at +
                    '}';
        }
    }

    /**
     * Retrieves a moderation by its ID.
     *
     * @param moderationId The ID of the moderation to retrieve.
     * @return The {@link Moderation} record.
     */
    public static Moderation getModeration(long moderationId) {
        return Query.query("SELECT * FROM moderations WHERE id = ?")
                .single(Call.of().bind(moderationId))
                .mapAs(Moderation.class)
                .first().orElseThrow();
    }

    /**
     * Creates a new moderation record in the database.
     *
     * @param userId           The ID of the user being moderated.
     * @param type             The type of moderation.
     * @param issuerId         The ID of the user issuing the moderation.
     * @param reason           The reason for the moderation.
     * @param paragraph        The rule paragraph associated with the moderation.
     * @param referenceMessage The reference message associated with the moderation.
     * @param duration         The duration of the moderation.
     * @return The {@link InsertionResult} (with keys) of the operation.
     */
    public static InsertionResult createModeration(
            long userId,
            ModerationType type,
            long issuerId,
            @Nullable String reason,
            @Nullable RuleService.RuleParagraph paragraph,
            @Nullable MessageReferenceService.MessageReference referenceMessage,
            @Nullable Long duration
    ) {
        Optional<Timestamp> revoke_at = duration == null ? Optional.empty() : Optional.of(new Timestamp(System.currentTimeMillis() + duration));

        return Query.query("INSERT INTO moderations (user_id, type, issuer_id, reason, paragraph_id, reference_message, revoke_at, duration) VALUES (?, ?::reporttype, ?, ?, ?, ?, ?, ?)")
                .single(Call.of()
                        .bind(userId)
                        .bind(type)
                        .bind(issuerId)
                        .bind(reason)
                        .bind(paragraph == null ? null : paragraph.id())
                        .bind(referenceMessage == null ? null : referenceMessage.messageId())
                        .bind(revoke_at.orElse(null))
                        .bind(duration)
                ).insertAndGetKeys();
    }

    /**
     * Deletes a moderation record from the database.
     *
     * @param moderationId The ID of the moderation to delete.
     */
    public static void deleteModeration(long moderationId) {
        Query.query("DELETE FROM moderations WHERE id = ?")
                .single(Call.call().bind(moderationId))
                .delete();
    }
}
