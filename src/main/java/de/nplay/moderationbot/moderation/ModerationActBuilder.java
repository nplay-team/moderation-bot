package de.nplay.moderationbot.moderation;

import de.nplay.moderationbot.rules.RuleService;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Builder class for creating instances of {@link ModerationService.ModerationAct}.
 */
public class ModerationActBuilder {
    private long id;
    private long userId;
    private ModerationActType type;
    private Boolean reverted = false;
    private String reason;
    private RuleService.RuleParagraph paragraph;
    private MessageReferenceService.MessageReference referenceMessage;
    private Timestamp revokeAt;
    private Long duration;
    private long issuerId;
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    /**
     * Sets the ID of the moderation act.
     *
     * @param id the ID to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setId(long id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the user ID of the moderation act.
     *
     * @param userId the user ID to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Sets the type of the moderation act.
     *
     * @param type the type to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setType(ModerationActType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets whether the moderation act is reverted.
     *
     * @param reverted the reverted status to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setReverted(Boolean reverted) {
        this.reverted = reverted;
        return this;
    }

    /**
     * Sets the reason for the moderation act.
     *
     * @param reason the reason to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setReason(String reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Sets the rule paragraph associated with the moderation act.
     *
     * @param paragraph the rule paragraph to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setParagraph(RuleService.RuleParagraph paragraph) {
        this.paragraph = paragraph;
        return this;
    }

    /**
     * Sets the reference message associated with the moderation act.
     *
     * @param referenceMessage the reference message to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setReferenceMessage(MessageReferenceService.MessageReference referenceMessage) {
        this.referenceMessage = referenceMessage;
        return this;
    }

    /**
     * Sets the timestamp when the moderation act will be revoked.
     *
     * @param revokeAt the revoke timestamp to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setRevokeAt(Timestamp revokeAt) {
        this.revokeAt = revokeAt;
        return this;
    }

    /**
     * Sets the duration of the moderation act.
     *
     * @param duration the duration to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setDuration(Long duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Sets the ID of the issuer of the moderation act.
     *
     * @param issuerId the issuer ID to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setIssuerId(long issuerId) {
        this.issuerId = issuerId;
        return this;
    }

    /**
     * Sets the creation date of the moderation act.
     *
     * @param createdAt the creation date to set
     * @return the current instance of {@link ModerationActBuilder}
     */
    public ModerationActBuilder setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Builds and returns a new {@link ModerationService.ModerationAct} instance.
     *
     * @return a new instance of {@link ModerationService.ModerationAct}
     */
    public ModerationService.ModerationAct build() {
        Optional<Timestamp> revokeAt = Optional.ofNullable(
                this.revokeAt != null ?
                        this.revokeAt :
                        (duration != null ? new Timestamp(System.currentTimeMillis() + duration) : null)
        );

        return new ModerationService.ModerationAct(
                id, userId, type, reverted, Optional.ofNullable(reason),
                Optional.ofNullable(paragraph), Optional.ofNullable(referenceMessage),
                revokeAt, Optional.ofNullable(duration), issuerId, createdAt
        );
    }
}
