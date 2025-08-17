package de.nplay.moderationbot.moderation.create;

import de.nplay.moderationbot.moderation.ModerationActType;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Builder class for creating instances of {@link ModerationService.ModerationAct} used for creating a new moderation action.
 */
public class ModerationActBuilder {

    private static final Logger log = LoggerFactory.getLogger(ModerationActBuilder.class);
    private Consumer<ModerationActCreateData> executor;
    private long targetId;
    private ModerationActType type;
    private String reason;
    private Integer paragraphId;
    private Message messageReference;
    private long duration;
    private int deletionDays;
    private long issuerId;

    public static ModerationActBuilder warn(Member target, User issuer) {
        return new ModerationActBuilder().issuer(issuer)
                .type(ModerationActType.WARN)
                .target(target)
                .executor(_ -> log.info("User {} has been warned by {}", target, issuer));
    }

    public static ModerationActBuilder timeout(Member target, User issuer) {
        return new ModerationActBuilder().issuer(issuer)
                .type(ModerationActType.TIMEOUT)
                .target(target)
                .executor(data -> {
                    if (data.revokeAt().isEmpty()) {
                        throw new IllegalStateException("Cannot perform timeout without duration being set!");
                    }
                    log.info("User {} has been timed out by {} until {}", target, issuer, data.duration());
                    target.timeoutUntil(data.revokeAt().get().toInstant()).reason(data.reason()).queue();
                });
    }

    public static ModerationActBuilder kick(Member target, User issuer) {
        return new ModerationActBuilder().issuer(issuer)
                .type(ModerationActType.KICK)
                .target(target)
                .executor(data -> {
                    log.info("User {} has been kicked by {}", target, issuer);
                    if (data.deletionDays > 0) {
                        target.ban(data.deletionDays(), TimeUnit.DAYS).queue(_ -> {
                            target.getGuild().unban(target.getUser()).queue();
                        });
                    } else {
                        target.kick().reason(data.reason()).queue();
                    }
                });
    }

    public static ModerationActBuilder ban(Member target, User issuer) {
        return new ModerationActBuilder().issuer(issuer)
                .type(ModerationActType.BAN)
                .target(target)
                .executor(data -> {
                    log.info("User {} has been{} banned by {}", target, data.revokeAt().isPresent() ? " temp" : "", issuer);
                    target.ban(data.deletionDays(), TimeUnit.DAYS).reason(data.reason()).queue();
                });
    }

    public static ModerationActBuilder ban(User target, Guild guild, User issuer) {
        return new ModerationActBuilder().issuer(issuer)
                .type(ModerationActType.BAN)
                .target(target)
                .executor(data -> {
                    log.info("User {} has been{} banned by {}", target, data.revokeAt().isPresent() ? " temp" : "", issuer);
                    guild.ban(target, data.deletionDays(), TimeUnit.DAYS).reason(data.reason()).queue();
                });
    }

    public ModerationActBuilder issuer(@NotNull UserSnowflake issuer) {
        this.issuerId = issuer.getIdLong();
        return this;
    }

    public ModerationActBuilder type(@NotNull ModerationActType type) {
        this.type = type;
        return this;
    }

    public ModerationActBuilder target(@NotNull UserSnowflake user) {
        this.targetId = user.getIdLong();
        return this;
    }

    public ModerationActBuilder executor(@NotNull Consumer<ModerationActCreateData> consumer) {
        this.executor = consumer;
        return this;
    }

    public ModerationActBuilder reason(@NotNull String reason) {
        this.reason = reason;
        return this;
    }

    public ModerationActBuilder paragraph(@Nullable String paragraphId) {
        if (paragraphId == null) {
            return this;
        }
        try {
            this.paragraphId = Integer.parseInt(paragraphId);
        } catch (NumberFormatException e) {
            this.paragraphId = RuleService.getRuleParagraphByDisplayName(paragraphId).map(RuleService.RuleParagraph::id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid paragraph ID or name: " + paragraphId));
        }
        return this;
    }

    public ModerationActBuilder messageReference(@Nullable Message message) {
        this.messageReference = message;
        return this;
    }

    /// Sets the duration of the moderation act. This is only applicable for acts of type [#TIMEOUT] or [#TEMP_BAN]
    public ModerationActBuilder duration(long duration) {
        if (type == ModerationActType.TIMEOUT || type == ModerationActType.TEMP_BAN) {
            this.duration = duration;
            return this;
        }
        throw new UnsupportedOperationException("Cannot set duration on moderation act with type: " + type);
    }

    public ModerationActBuilder deletionDays(int days) {
        if (type == ModerationActType.BAN || type == ModerationActType.TEMP_BAN || type == ModerationActType.KICK) {
            deletionDays = days;
            return this;
        }
        throw new UnsupportedOperationException("Cannot set deletion days on moderation act with type: " + type);
    }

    public ModerationActCreateData build() {
        return new ModerationActCreateData(targetId, type, issuerId, reason, messageReference, paragraphId, duration, deletionDays, executor);
    }

    public record ModerationActCreateData(
            long targetId,
            @NotNull ModerationActType type,
            long issuerId,
            @Nullable String reason,
            @Nullable Message messageReference,
            @Nullable Integer paragraphId,
            long duration,
            int deletionDays,
            @NotNull Consumer<ModerationActCreateData> executor
    ) {

        public ModerationActCreateData {
            Checks.isSnowflake(targetId + "", "targetId");
            Checks.notNull(type, "ModerationActType");
            Checks.isSnowflake(issuerId + "", "issuerId");
            Checks.notNull(executor, "ModerationActCreateData Consumer");
        }

        public Optional<Timestamp> revokeAt() {
            return duration == 0 ? Optional.empty() : Optional.of(new Timestamp(System.currentTimeMillis() + duration));
        }

        public Optional<Long> messageReferenceId() {
            return Optional.ofNullable(messageReference).map(ISnowflake::getIdLong);
        }

    }
}
