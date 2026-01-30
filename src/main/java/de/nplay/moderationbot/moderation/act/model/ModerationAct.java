package de.nplay.moderationbot.moderation.act.model;

import net.dv8tion.jda.api.entities.UserSnowflake;
import de.chojo.sadu.mapper.wrapper.Row;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.Replies.RelativeTime;
import de.nplay.moderationbot.moderation.MessageReferenceService;
import de.nplay.moderationbot.moderation.MessageReferenceService.MessageReference;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder.ModerationActType;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Optional;


public sealed class ModerationAct permits RevertedModerationAct {

    private final long id;
    private final UserSnowflake user;
    private final ModerationActType type;
    private final UserSnowflake issuer;
    private final Timestamp createdAt;
    private final String reason;
    private final @Nullable RuleParagraph paragraph;
    private final @Nullable MessageReference referenceMessage;
    private final @Nullable Timestamp revokeAt;
    private final long duration;

    public ModerationAct(Row row) throws SQLException {
        this.id = row.getLong("id");
        this.user = UserSnowflake.fromId(row.getLong("user_id"));
        this.type = ModerationActType.valueOf(row.getString("type"));
        this.issuer = UserSnowflake.fromId(row.getLong("issuer_id"));
        this.createdAt = row.getTimestamp("created_at");
        this.reason = row.getString("reason");
        this.paragraph = RuleService.getRuleParagraph(row.getInt("paragraph_id")).orElse(null);
        this.referenceMessage = MessageReferenceService.getMessageReference(row.getLong("reference_message")).orElse(null);
        this.revokeAt = row.getTimestamp("revoke_at");
        this.duration = row.getLong("duration");
    }

    public long id() {
        return id;
    }

    public UserSnowflake user() {
        return user;
    }

    public ModerationActType type() {
        return type;
    }

    public UserSnowflake issuer() {
        return issuer;
    }

    public AbsoluteTime createdAt() {
        return new AbsoluteTime(createdAt);
    }

    public String reason() {
        return reason;
    }

    public Optional<@Nullable RuleParagraph> paragraph() {
        return Optional.ofNullable(paragraph);
    }

    public Optional<@Nullable MessageReference> referenceMessage() {
        return Optional.ofNullable(referenceMessage);
    }

    public Optional<@Nullable RelativeTime> revokeAt() {
        return Optional.ofNullable(revokeAt).map(RelativeTime::new);
    }

    public Duration duration() {
        return Duration.ofMillis(duration);
    }
}
