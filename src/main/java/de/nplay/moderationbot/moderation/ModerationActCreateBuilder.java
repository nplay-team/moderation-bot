package de.nplay.moderationbot.moderation;

import de.nplay.moderationbot.rules.RuleService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Builder class for creating instances of {@link ModerationService.ModerationAct} used for creating a new moderation action.
 */
public class ModerationActCreateBuilder {
    private long id;
    private long userId;
    private ModerationActType type;
    private String reason;
    private RuleService.RuleParagraph paragraph;
    private MessageReferenceService.MessageReference referenceMessage;
    private long duration;
    private long issuerId;
    private int delDays;

    public ModerationActCreateBuilder setId(long id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the user ID of the moderation act.
     *
     * @param user the {@link net.dv8tion.jda.api.entities.UserSnowflake User or Member} to perform the action on
     * @return the current instance of {@link ModerationActCreateBuilder}
     */
    public ModerationActCreateBuilder setUser(UserSnowflake user) {
        this.userId = user.getIdLong();
        return this;
    }

    /**
     * Sets the type of the moderation act.
     *
     * @param type the type to set
     * @return the current instance of {@link ModerationActCreateBuilder}
     */
    public ModerationActCreateBuilder setType(@NotNull ModerationActType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the reason for the moderation act.
     *
     * @param reason the reason to set
     * @return the current instance of {@link ModerationActCreateBuilder}
     */
    public ModerationActCreateBuilder setReason(@Nullable String reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Sets the rule paragraph associated with the moderation act.
     *
     * @param paragraph the rule paragraph to set
     * @return the current instance of {@link ModerationActCreateBuilder}
     */
    public ModerationActCreateBuilder setParagraph(@Nullable RuleService.RuleParagraph paragraph) {
        this.paragraph = paragraph;
        return this;
    }

    /**
     * Sets the message reference associated with the moderation act.
     *
     * @param message the referenced {@link Message}
     * @return the current instance of {@link ModerationActCreateBuilder}
     */
    public ModerationActCreateBuilder setMessageReference(Message message) {
        this.referenceMessage = new MessageReferenceService.MessageReference(
                message.getIdLong(),
                message.getChannelIdLong(),
                Optional.of(message.getContentRaw())
        );
        return this;
    }

    /**
     * Sets the duration of the moderation act. This is only applicable for acts of type
     * {@link ModerationActType#TIMEOUT} or {@link ModerationActType#TEMP_BAN}
     *
     * @param duration the duration to set
     * @return the current instance of {@link ModerationActCreateBuilder}
     */
    public ModerationActCreateBuilder setDuration(long duration) {
        if (type == ModerationActType.TIMEOUT || type == ModerationActType.TEMP_BAN) {
            this.duration = duration;
            return this;
        }
        throw new IllegalArgumentException("Cannot set duration on moderation act with type: " + type);
    }

    /**
     * Sets the ID of the issuer of the moderation act.
     *
     * @param issuer the {@link UserSnowflake Member or User} issuing the action
     * @return the current instance of {@link ModerationActCreateBuilder}
     */
    public ModerationActCreateBuilder setIssuer(UserSnowflake issuer) {
        this.issuerId = issuer.getIdLong();
        return this;
    }

    /**
     * Sets the number of days to delete messages for the moderation act.
     * Warning: This value is not stored in DB and is only available runtime.
     *
     * @param delDays the number of days to delete messages
     *                (0 to 7, 0 means no deletion)
     */
    public ModerationActCreateBuilder setDelDays(int delDays) {
        this.delDays = delDays;
        return this;
    }

    /**
     * Creates a new moderation record in the database.
     *
     * @return The id of the moderation act
     */
    public long create() {
        if (referenceMessage != null) {
            MessageReferenceService.createMessageReference(referenceMessage);
        }
        return ModerationService.createModerationAct(build());
    }

    /**
     * Builds and returns a new {@link ModerationService.ModerationAct} instance.
     *
     * @return a new instance of {@link ModerationService.ModerationAct}
     */
    public ModerationService.ModerationAct build() {
        return new ModerationService.ModerationAct(
                Optional.ofNullable(id).orElse(-1L),
                userId,
                type,
                false,
                Optional.ofNullable(reason),
                Optional.ofNullable(paragraph),
                Optional.ofNullable(referenceMessage),
                duration == 0 ? Optional.empty() : Optional.of(new Timestamp(System.currentTimeMillis() + duration)),
                duration == 0 ? Optional.empty() : Optional.of(duration),
                issuerId,
                new Timestamp(System.currentTimeMillis()),
                Optional.ofNullable(delDays)
        );
    }
}
