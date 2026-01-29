package de.nplay.moderationbot.moderation.act.model;

import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.message.resolver.Resolver;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.Replies.RelativeTime;
import de.nplay.moderationbot.moderation.MessageReferenceService;
import de.nplay.moderationbot.moderation.MessageReferenceService.MessageReference;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder.ModerationActType;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import de.nplay.moderationbot.util.SeparatedContainer;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

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

    public RevertedModerationAct revert(ReplyableEvent<?> event, String reason) {
        return revert(event.getGuild(), event.getUser(), reason, event.getUserLocale());
    }

    public RevertedModerationAct automaticRevert(Guild guild, Resolver<String> resolver) {
        return revert(guild, guild.getJDA().getSelfUser(), resolver.resolve("automatic-revert-reason", DiscordLocale.GERMAN), DiscordLocale.GERMAN);
    }

    private RevertedModerationAct revert(Guild guild, User revertedBy, String reason, DiscordLocale locale) {
        if (this instanceof RevertedModerationAct reverted) {
            return reverted;
        }

        Query.query("UPDATE moderations SET reverted = true, reverted_by = ?, reverted_at = ?, revert_reason = ? WHERE id = ?")
                .single(Call.of()
                        .bind(revertedBy.getIdLong())
                        .bind(new Timestamp(System.currentTimeMillis()))
                        .bind(reason)
                        .bind(id))
                .update();

        switch (type) {
            case BAN, TEMP_BAN -> Helpers.complete(guild.unban(user));
            case TIMEOUT -> Helpers.complete(guild.retrieveMember(user).flatMap(Member::removeTimeout));
        }

        sendRevertMessageToUser(guild, revertedBy, reason, locale);
        return (RevertedModerationAct) ModerationActService.get(id).orElseThrow();
    }

    private void sendRevertMessageToUser(Guild guild, User revertedBy, String reason, DiscordLocale locale) {
        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("revert$revert-info"),
                Separator.createDivider(Separator.Spacing.SMALL),
                entry("type", type.localized(locale))
        ).withAccentColor(Replies.SUCCESS);
        container.append(
                TextDisplay.of("revert$revert-info.body"),
                entry("id", id),
                entry("date", createdAt()),
                entry("reason", reason)
        );
        container.append(TextDisplay.of("revert$revert-info.reverter"), entry("revertedBy", revertedBy));

        Helpers.sendDM(user, guild.getJDA(), channel -> channel.sendMessageComponents(container).useComponentsV2());
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
