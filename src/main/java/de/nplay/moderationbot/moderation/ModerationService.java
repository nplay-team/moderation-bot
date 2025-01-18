package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapper;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Bootstrapper;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for managing moderation acts.
 */
public class ModerationService {
    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    /**
     * Warns a member
     *
     * @param user the {@link UserSnowflake User or Member} to warn
     * @return a {@link ModerationActCreateBuilder}
     */
    public static ModerationActCreateBuilder warn(UserSnowflake user) {
        return new ModerationActCreateBuilder().setUser(user).setType(ModerationActType.WARN);
    }

    /**
     * Timeouts a member
     *
     * @param user the {@link UserSnowflake User or Member} to timeout
     * @return a {@link ModerationActCreateBuilder}
     */
    public static ModerationActCreateBuilder timeout(UserSnowflake user) {
        return new ModerationActCreateBuilder().setUser(user).setType(ModerationActType.TIMEOUT);
    }

    /**
     * Kicks a member
     *
     * @param user the {@link UserSnowflake User or Member} to kick
     * @return a {@link ModerationActCreateBuilder}
     */
    public static ModerationActCreateBuilder kick(UserSnowflake user) {
        return new ModerationActCreateBuilder().setUser(user).setType(ModerationActType.KICK);
    }

    /**
     * Temp bans a member
     *
     * @param user the {@link UserSnowflake User or Member} to temp ban
     * @return a {@link ModerationActCreateBuilder}
     */
    public static ModerationActCreateBuilder tempBan(UserSnowflake user) {
        return new ModerationActCreateBuilder().setUser(user).setType(ModerationActType.TEMP_BAN);
    }

    /**
     * Bans a member
     *
     * @param user the {@link UserSnowflake User or Member} to ban
     * @return a {@link ModerationActCreateBuilder}
     */
    public static ModerationActCreateBuilder ban(UserSnowflake user) {
        return new ModerationActCreateBuilder().setUser(user).setType(ModerationActType.BAN);
    }

    /**
     * Creates a new moderation record in the database.
     *
     * @param act The {@link ModerationAct} to create.
     * @return The id of the moderation act
     */
    public static long createModerationAct(ModerationAct act) {
        return Query.query("""
                INSERT INTO moderations
                (user_id, type, reverted, issuer_id, reason, paragraph_id, reference_message, revoke_at, duration, created_at)
                VALUES (?, ?::reporttype, ?, ?, ?, ?, ?, ?, ?, ?)
                """
        ).single(Call.of()
                .bind(act.userId)
                .bind(act.type)
                .bind(act.reverted)
                .bind(act.issuerId)
                .bind(act.reason.orElse(null))
                .bind(act.paragraph.map(RuleService.RuleParagraph::id).orElse(null))
                .bind(act.referenceMessage.map(MessageReferenceService.MessageReference::messageId).orElse(null))
                .bind(act.revokeAt.orElse(null))
                .bind(act.duration.orElse(null))
                .bind(act.created_at)
        ).insertAndGetKeys().keys().getFirst();
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

        /**
         * Executes the moderation action.
         */
        public void execute(EmbedCache embedCache) {
            logger.info("Executing moderation action: {}", this);
            var issuer = Bootstrapper.bot.getJda().getUserById(issuerId);
            var member = Bootstrapper.bot.getGuild().retrieveMemberById(userId).complete();

            sendMessageToUser(embedCache, issuer, member.getUser());

            switch (type) {
                case WARN -> {
                    logger.info("User {} has been warned by {}", userId, issuerId);
                }

                case TIMEOUT -> {
                    logger.info("User {} has been timed out by {} until {}", userId, issuerId, revokeAt.orElse(null));
                    member.timeoutUntil(revokeAt.get().toInstant()).reason(reason.orElse(null)).queue();
                }

                case KICK -> {
                    logger.info("User {} has been kicked by {}", userId, issuerId);
                    member.kick().reason(reason.orElse(null)).queue();
                }

                case TEMP_BAN -> {
                    logger.info("User {} has been temp banned by {}", userId, issuerId);
                    member.ban(delDays.orElse(0), TimeUnit.DAYS).reason(reason.orElse(null)).queue();
                }

                case BAN -> {
                    logger.info("User {} has been banned by {}", userId, issuerId);
                    member.ban(delDays.orElse(0), TimeUnit.DAYS).reason(reason.orElse(null)).queue();
                }
            }
        }

        private void sendMessageToUser(EmbedCache embedCache, User issuer, User user) {
            var issuerId = issuer != null ? issuer.getId() : Bootstrapper.bot.getJda().getSelfUser().getId();
            var issuerUsername = issuer != null ? issuer.getName() : "System";

            Map<String, Object> defaultInjectValues = new HashMap<>();
            defaultInjectValues.put("issuerId", issuerId);
            defaultInjectValues.put("issuerUsername", issuerUsername);
            defaultInjectValues.put("reason", reason.orElse("?DEL?"));
            defaultInjectValues.put("date", created_at.getTime() / 1000);
            defaultInjectValues.put("paragraph", paragraph.map(ruleParagraph -> ruleParagraph + "\n" + ruleParagraph.content().orElse("/")).orElse("?DEL?"));
            defaultInjectValues.put("id", id);

            if (user != null) {
                EmbedDTO embedDTO = null;

                switch (type) {
                    case WARN -> {
                        embedDTO = embedCache.getEmbed("warnEmbed").injectValues(defaultInjectValues).injectValue("color", EmbedColors.WARNING);
                    }

                    case TIMEOUT -> {
                        embedDTO = embedCache.getEmbed("timeoutEmbed").injectValues(defaultInjectValues).injectValue("until", revokeAt.get().getTime() / 1000).injectValue("color", EmbedColors.WARNING);
                    }

                    case KICK -> {
                        embedDTO = embedCache.getEmbed("kickEmbed").injectValues(defaultInjectValues).injectValue("color", EmbedColors.ERROR);
                    }

                    case TEMP_BAN -> {
                        embedDTO = embedCache.getEmbed("tempBanEmbed").injectValues(defaultInjectValues).injectValue("until", revokeAt.get().getTime() / 1000).injectValue("color", EmbedColors.ERROR);
                    }

                    case BAN -> {
                        embedDTO = embedCache.getEmbed("banEmbed").injectValues(defaultInjectValues).injectValue("color", EmbedColors.ERROR);
                    }
                }

                if (embedDTO != null) {
                    EmbedBuilder embedBuilder = embedDTO.toEmbedBuilder();
                    embedBuilder.getFields().removeIf(it -> "?DEL?".equals(it.getValue()));
                    user.openPrivateChannel().flatMap(it -> it.sendMessageEmbeds(embedBuilder.build())).complete();
                }
            }
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
}
