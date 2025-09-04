package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.embeds.Embed;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapper;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.NPLAYModerationBot;
import de.nplay.moderationbot.moderation.act.ModerationActType;
import de.nplay.moderationbot.moderation.act.ModerationActBuilder.ModerationActCreateData;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;
import static de.nplay.moderationbot.Helpers.USER_HANDLER;

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
                .bind(Optional.ofNullable(data.duration()).map(Duration::toMillis).orElse(0L))
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
        return getModerationActs(user, null, null);
    }

    public static List<ModerationAct> getModerationActs(UserSnowflake user,
                                                        @Nullable Integer limit, @Nullable Integer offset) {
        return Query.query("SELECT * FROM moderations WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?")
                .single(Call.of()
                        .bind(user.getIdLong())
                        .bind(limit == null ? 25 : limit)
                        .bind(offset == null ? 0 : offset)
                )
                .mapAs(ModerationAct.class)
                .all();
    }

    public static Integer getModerationActCount(UserSnowflake user) {
        return Query.query("SELECT COUNT(*) FROM moderations WHERE user_id = ?")
                .single(Call.of().bind(user.getIdLong()))
                .mapAs(Integer.class)
                .first().orElse(0);
    }

    /**
     * Updates an existing moderation record in the database.
     *
     * @param act The {@link ModerationAct} to update.
     */
    public static ModerationAct updateModerationAct(ModerationAct act) {
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

        return getModerationAct(act.id).orElseThrow();
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

    public static boolean isTimeOuted(long userId) {
        return Query.query("SELECT EXISTS(SELECT 1 FROM moderations WHERE user_id = ? AND TYPE = 'TIMEOUT' AND reverted = FALSE)")
                .single(Call.of().bind(userId))
                .map(row -> row.getBoolean(1))
                .first().orElse(false);
    }

    public static boolean isBanned(long userId) {
        return Query.query("SELECT EXISTS(SELECT 1 FROM moderations WHERE user_id = ? AND TYPE IN ('BAN', 'TEMP_BAN') AND reverted = FALSE)")
                .single(Call.of().bind(userId))
                .map(row -> row.getBoolean(1))
                .first().orElse(false);
    }

    public record ModerationAct(
            long id,
            long userId,
            ModerationActType type,
            boolean reverted,
            @Nullable Long revertedBy,
            @Nullable Timestamp revertedAt,
            @Nullable String revertingReason,
            @Nullable String reason,
            @Nullable RuleParagraph paragraph,
            MessageReferenceService.@Nullable MessageReference referenceMessage,
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
                    row.getInt("paragraph_id") != 0 ? RuleService.getRuleParagraph(row.getInt("paragraph_id")).orElse(null) : null,
                    row.getLong("reference_message") != 0 ? MessageReferenceService.getMessageReference(row.getLong("reference_message")).orElse(null) : null,
                    row.getTimestamp("revoke_at"),
                    row.getLong("duration") == 0 ? null : row.getLong("duration"),
                    row.getLong("issuer_id"),
                    row.getTimestamp("created_at"),
                    null
            );
        }

        public ModerationAct revert(Guild guild, Function<String, Embed> embedFunction, User revertedBy, @Nullable String reason) {
            if (reverted) {
                return this;
            }
            log.info("Reverting moderation action: {}", this);

            Query.query("UPDATE moderations SET reverted = true, reverted_by = ?, reverted_at = ?, revert_reason = ? WHERE id = ?")
                    .single(Call.of()
                            .bind(revertedBy.getIdLong())
                            .bind(new Timestamp(System.currentTimeMillis()))
                            .bind(reason)
                            .bind(id))
                    .update();

            switch (type) {
                case BAN, TEMP_BAN -> guild.unban(UserSnowflake.fromId(String.valueOf(userId))).queue(_ -> {
                }, USER_HANDLER);
                case TIMEOUT -> {
                    guild.retrieveMemberById(userId).flatMap(Member::removeTimeout).queue(_ -> {
                    }, USER_HANDLER);
                    sendRevertMessageToUser(guild, embedFunction, revertedBy, reason);
                }
                case WARN -> sendRevertMessageToUser(guild, embedFunction, revertedBy, reason);
            }

            return getModerationAct(id).orElseThrow();
        }

        private void sendRevertMessageToUser(Guild guild, Function<String, Embed> embedFunction, User revertedBy, @Nullable String revertingReason) {
            var embed = embedFunction.apply(type == ModerationActType.TIMEOUT ? "timeoutReverted" : "warnReverted").placeholders(
                    entry("date", createdAt.getTime() / 1000),
                    entry("id", id),
                    entry("reason", Objects.requireNonNullElse(revertingReason, "?DEL?")),
                    entry("revertedById", revertedBy.getIdLong()),
                    entry("revertedByUsername", revertedBy.getName()));

            embed.getFields().removeIf(it -> "?DEL?".equals(it.getValue()));

            guild.retrieveMemberById(userId).flatMap(it -> it.getUser().openPrivateChannel())
                    .flatMap(channel -> channel.sendMessageEmbeds(embed.build()))
                    .queue(_ -> {
                    }, USER_HANDLER);
        }

        public Field toField(JDA jda) {
            String headLine = "#%s | %s | <t:%s>".formatted(id, type, createdAt.getTime() / 1000);
            List<String> bodyLines = new ArrayList<>();

            bodyLines.add("%s".formatted(reason));
            bodyLines.add("-<@%s> (%s)".formatted(issuerId, jda.retrieveUserById(issuerId).complete().getName()));

            if (revokeAt != null && !reverted) {
                bodyLines.addFirst("Aktiv bis: <t:%s:f>".formatted(revokeAt.getTime() / 1000));
            }

            if (duration != null) {
                bodyLines.addFirst("Dauer: %s".formatted(Helpers.durationToString(Duration.ofMillis(duration))));
            }

            if (reverted) {
                if (revertedBy == null || revertedBy != jda.getSelfUser().getIdLong()) {
                    headLine = "~~%s~~".formatted(headLine);
                    bodyLines.forEach(it -> bodyLines.set(bodyLines.indexOf(it), "~~%s~~".formatted(it)));
                }

                bodyLines.addLast("*Aufgehoben am: <t:%s:f>*".formatted(revertedAt.getTime() / 1000));
            }

            return new Field(
                    headLine,
                    String.join("\n", bodyLines),
                    false
            );
        }

        public Embed getEmbed(ReplyableEvent<?> event, JDA jda, Guild guild) {
            var embed = event.embed("moderationActDetail");
            embed.placeholders(
                    entry("id", id),
                    entry("type", type),
                    entry("date", createdAt.getTime() / 1000),
                    entry("issuerId", issuerId),
                    entry("issuerUsername", jda.retrieveUserById(issuerId).complete().getName()),
                    entry("reason", reason == null ? "?DEL?" : reason),
                    entry("paragraph", paragraph == null ? "?DEL?" : paragraph.fullDisplay()),
                    entry("duration", duration == null ? "?DEL?" : Helpers.durationToString(Duration.ofMillis(duration))),
                    entry("referenceMessage", referenceMessage == null ? "?DEL?" : Matcher.quoteReplacement(referenceMessage.jumpUrl(guild))),
                    entry("until", revokeAt == null ? "?DEL?" : revokeAt.getTime() / 1000),
                    entry("revertedAt", revertedAt == null ? "?DEL?" : revertedAt.getTime() / 1000),
                    entry("revertedById", revertedBy == null ? "?DEL?" : revertedBy),
                    entry("revertedByUsername", revertedBy == null ? "?DEL?" : jda.retrieveUserById(revertedBy).complete().getName()),
                    entry("reversionReason", revertingReason == null ? "?DEL?" : revertingReason),
                    entry("color", NPLAYModerationBot.EmbedColors.DEFAULT)
            );
            embed.fields().removeIf(it -> it.getValue().contains("?DEL?"));
            return embed;
        }
    }
}
