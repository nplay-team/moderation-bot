package de.nplay.moderationbot.moderation;

import de.nplay.moderationbot.moderation.MessageReferenceService.MessageReference;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
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

    public static ModerationActBuilder warn(Member target, User issuer) {
        return new ModerationActBuilder().issuer(issuer)
                .type(ModerationActType.WARN)
                .target(issuer)
                .action(_ -> log.info("User {} has been warned by {}", target, issuer));
    }

    public static ModerationActBuilder timeout(Member target, User issuer) {
        return new ModerationActBuilder().issuer(issuer)
                .type(ModerationActType.TIMEOUT)
                .target(target)
                .action(action -> {
                    if (action.revokeAt().isEmpty()) {
                        throw new IllegalStateException("Cannot perform timeout without TemporaryCreateAction");
                    }
                    log.info("User {} has been timed out by {} until {}", target, issuer, action.duration());
                    target.timeoutUntil(action.revokeAt().get().toInstant()).reason(action.reason()).queue();
                });
    }

    public static ModerationActBuilder kick(Member target, User issuer) {
        return new ModerationActBuilder()
                .target(target)
                .type(ModerationActType.KICK)
                .issuer(issuer)
                .action(action -> {
                    log.info("User {} has been kicked by {}", target, issuer);
                    target.kick().reason(action.reason()).queue();
                });
    }

    public static ModerationActBuilder tempBan(Member target, User issuer, @Nullable Integer delDays) {
        return new ModerationActBuilder()
                .target(target)
                .type(ModerationActType.TEMP_BAN)
                .issuer(issuer)
                .action(action -> {
                    log.info("User {} has been temp banned by {}", target, issuer);
                    target.ban(delDays == null ? 0 : delDays, TimeUnit.DAYS).reason(action.reason()).queue();
                });
    }

    public static ModerationActBuilder ban(Member target, User issuer, @Nullable Integer delDays) {
        return new ModerationActBuilder()
                .target(target)
                .type(ModerationActType.BAN)
                .issuer(issuer)
                .action(action -> {
                    log.info("User {} has been banned by {}", target, issuer);
                    target.ban(delDays == null ? 0 : delDays, TimeUnit.DAYS).reason(action.reason()).queue();
                });
    }

    public record ModerationActCreateAction(
            long targetId,
            @NotNull ModerationActType type,
            long issuerId,
            @Nullable String reason,
            @Nullable MessageReference messageReference,
            @Nullable Integer paragraphId,
            @Nullable Long duration,
            @NotNull Consumer<ModerationActCreateAction> consumer) {

        public Optional<Timestamp> revokeAt() {
            return duration == null ? Optional.empty() : Optional.of(new Timestamp(System.currentTimeMillis() + duration));
        }

    }

    private Consumer<ModerationActCreateAction> consumer;
    private long targetId;
    private ModerationActType type;
    private String reason;
    private Integer paragraphId;
    private MessageReference messageReference;
    private long duration;
    private long issuerId;

    public ModerationActBuilder issuer(UserSnowflake issuer) {
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

    public ModerationActBuilder action(Consumer<ModerationActCreateAction> consumer) {
        this.consumer = consumer;
        return this;
    }

    public ModerationActBuilder reason(@Nullable String reason) {
        this.reason = reason;
        return this;
    }

    public ModerationActBuilder paragraph(@Nullable String paragraphId) {
        if (paragraphId == null) {
            return this;
        }
        this.paragraphId = Integer.parseInt(paragraphId);
        return this;
    }

    public ModerationActBuilder messageReference(Message message) {
        this.messageReference = new MessageReference(
                message.getIdLong(),
                message.getChannelIdLong(),
                Optional.of(message.getContentRaw())
        );
        return this;
    }

    /// Sets the duration of the moderation act. This is only applicable for acts of type [#TIMEOUT] or [#TEMP_BAN]
    public ModerationActBuilder duration(long duration) {
        if (type == ModerationActType.TIMEOUT || type == ModerationActType.TEMP_BAN) {
            this.duration = duration;
            return this;
        }
        throw new IllegalArgumentException("Cannot set duration on moderation act with type: " + type);
    }

    public ModerationActCreateAction build() {
        return new ModerationActCreateAction(
                targetId,
                type,
                issuerId,
                reason,
                messageReference,
                paragraphId,
                duration,
                consumer
        );
    }
}
