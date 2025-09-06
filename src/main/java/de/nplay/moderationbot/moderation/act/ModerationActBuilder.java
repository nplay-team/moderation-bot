package de.nplay.moderationbot.moderation.act;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.moderation.act.ModerationActService.ModerationAct;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.Checks;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/// Builder class for creating instances of [ModerationAct] used for creating a new moderation action.
public class ModerationActBuilder {

    private static final Logger log = LoggerFactory.getLogger(ModerationActBuilder.class);
    private final long issuerId;
    private final long targetId;
    private final Consumer<ModerationActCreateData> executor;
    private ModerationActType type;
    @Nullable
    private String reason;
    @Nullable
    private Integer paragraphId;
    @Nullable
    private Message messageReference;
    @Nullable
    private Duration duration;
    private int deletionDays;

    private ModerationActBuilder(long issuerId, ModerationActType type, long targetId, Consumer<ModerationActCreateData> executor) {
        this.issuerId = issuerId;
        this.type = type;
        this.targetId = targetId;
        this.executor = executor;
    }

    public static ModerationActBuilder warn(UserSnowflake target, UserSnowflake issuer) {
        return new ModerationActBuilder(
                issuer.getIdLong(),
                ModerationActType.WARN,
                target.getIdLong(),
                _ -> log.info("User {} has been warned by {}", target, issuer)
        );
    }

    public static ModerationActBuilder timeout(Member target, UserSnowflake issuer) {
        return new ModerationActBuilder(
                issuer.getIdLong(),
                ModerationActType.TIMEOUT,
                target.getIdLong(),
                data -> {
                    if (data.revokeAt().isEmpty()) {
                        throw new IllegalStateException("Cannot perform timeout without duration being set!");
                    }
                    log.info("User {} has been timed out by {} until {}", target, issuer, data.duration());
                    target.timeoutUntil(data.revokeAt().get().toInstant()).reason(data.reason()).queue();
                }
        );
    }

    public static ModerationActBuilder kick(Member target, UserSnowflake issuer) {
        return new ModerationActBuilder(
                issuer.getIdLong(),
                ModerationActType.KICK,
                target.getIdLong(),
                data -> {
                    log.info("User {} has been kicked by {}", target, issuer);
                    if (data.deletionDays > 0) {
                        target.ban(data.deletionDays(), TimeUnit.DAYS).queue(_ -> {
                            target.getGuild().unban(target.getUser()).queue();
                        });
                    } else {
                        target.kick().reason(data.reason()).queue();
                    }
                }
        );
    }

    public static ModerationActBuilder ban(Member target, UserSnowflake issuer) {
        return ban(target.getUser(), target.getGuild(), issuer);
    }

    public static ModerationActBuilder ban(UserSnowflake target, Guild guild, UserSnowflake issuer) {
        return new ModerationActBuilder(
                issuer.getIdLong(),
                ModerationActType.BAN,
                target.getIdLong(),
                data -> {
                    log.info("User {} has been{} banned by {}", target, data.revokeAt().isPresent() ? " temp" : "", issuer);
                    guild.ban(target, data.deletionDays(), TimeUnit.DAYS).reason(data.reason()).queue();
                }
        );
    }

    public ModerationActBuilder reason(@Nullable String reason) {
        this.reason = reason;
        return this;
    }

    public ModerationActBuilder paragraph(@Nullable String paragraphId) {
        if (paragraphId == null) {
            return this;
        }
        try {
            this.paragraphId = Integer.parseInt(paragraphId);
        } catch (NumberFormatException _) {
            this.paragraphId = RuleService.getRuleParagraphByDisplayName(paragraphId)
                    .map(RuleService.RuleParagraph::id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid paragraph ID or name: " + paragraphId));
        }
        return this;
    }

    public ModerationActBuilder messageReference(@Nullable Message message) {
        this.messageReference = message;
        return this;
    }

    /// Sets the duration of the moderation act. This is only applicable for acts of type [ModerationActType#TIMEOUT]
    /// or [ModerationActType#TEMP_BAN].
    ///
    /// If this builder was of type [ModerationActType#BAN] will automatically transform this into a temp ban.
    public ModerationActBuilder duration(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Duration cannot be 0 or negative!");
        }
        if (type == ModerationActType.BAN) {
            type = ModerationActType.TEMP_BAN;
        }
        if (type == ModerationActType.TIMEOUT || type == ModerationActType.TEMP_BAN) {
            this.duration = duration;
            return this;
        }
        throw new UnsupportedOperationException("Cannot set duration on moderation act with type: " + type);
    }

    /// Sets the duration of the moderation act. This is only applicable for acts of type [ModerationActType#BAN],
    /// [ModerationActType#TEMP_BAN] or [ModerationActType#KICK]
    public ModerationActBuilder deletionDays(int days) {
        if (type == ModerationActType.BAN || type == ModerationActType.TEMP_BAN || type == ModerationActType.KICK) {
            deletionDays = days;
            return this;
        }
        throw new UnsupportedOperationException("Cannot set deletion days on moderation act with type: " + type);
    }

    public long issuerId() {
        return issuerId;
    }

    public long targetId() {
        return targetId;
    }

    public ModerationAct execute(ReplyableEvent<?> event) {
        var data = new ModerationActCreateData(targetId, type, issuerId, reason, messageReference, paragraphId, duration, deletionDays);
        ModerationAct act = ModerationActService.createModerationAct(data);
        executor.accept(data);
        Helpers.sendModerationToTarget(act, event);
        return act;
    }

    public record ModerationActCreateData(
            long targetId,
            ModerationActType type,
            long issuerId,
            @Nullable String reason,
            @Nullable Message messageReference,
            @Nullable Integer paragraphId,
            @Nullable Duration duration,
            int deletionDays
    ) {

        public ModerationActCreateData {
            Checks.isSnowflake(targetId + "", "targetId");
            Checks.notNull(type, "ModerationActType");
            Checks.isSnowflake(issuerId + "", "issuerId");
        }

        public Optional<Timestamp> revokeAt() {
            return Optional.ofNullable(duration).map(it -> new Timestamp(System.currentTimeMillis() + it.toMillis()));
        }

        public Optional<Long> messageReferenceId() {
            return Optional.ofNullable(messageReference).map(ISnowflake::getIdLong);
        }
    }

    public enum ModerationActType {
        WARN("Verwarnung"),
        TIMEOUT("Timeout"),
        KICK("Kick"),
        TEMP_BAN("Temporärer Bann"),
        BAN("Bann");

        private final String humanReadableString;

        ModerationActType(String humanReadableString) {
            this.humanReadableString = humanReadableString;
        }

        @Override
        public String toString() {
            return humanReadableString;
        }

        public boolean isBan() {
            return this == BAN || this == TEMP_BAN;
        }

        public boolean isTemp() {
            return this == TEMP_BAN || this == TIMEOUT;
        }
    }
}
