package de.nplay.moderationbot.moderation.act.model;

import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.NPLAYModerationBot.EmbedColors;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.Replies.RelativeTime;
import de.nplay.moderationbot.moderation.MessageReferenceService;
import de.nplay.moderationbot.moderation.MessageReferenceService.MessageReference;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder.ModerationActType;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.embeds.Embed;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.nplay.moderationbot.Helpers.formatTimestamp;
import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;
import static net.dv8tion.jda.api.utils.TimeFormat.DATE_TIME_SHORT;
import static net.dv8tion.jda.api.utils.TimeFormat.DEFAULT;

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

    public TextDisplay toTextDisplay(ReplyableEvent<?> event) {
        // TODO: add reverted case
        String headLine = "\\#%s | %s | %s".formatted(id, event.resolve(type.localizationKey()), DEFAULT.format(createdAt.getTime()));
        return TextDisplay.of("### %s\n%s\n-# Moderator: %s".formatted(headLine, reason, Helpers.formatUser(event.getJDA(), issuer)));
    }

    public Field toField(ReplyableEvent<?> event) {
        String headLine = "#%s | %s | %s".formatted(id, event.resolve(type.localizationKey()), DEFAULT.format(createdAt.getTime()));
        List<String> bodyLines = new ArrayList<>();

        bodyLines.add(reason);
        bodyLines.add("-%s".formatted(Helpers.formatUser(event.getJDA(), issuer)));

        if (this instanceof RevertedModerationAct reverted && !reverted.revertedBy().getId().equals(event.getJDA().getSelfUser().getId())) {
            headLine = "~~%s~~".formatted(headLine);
            bodyLines.forEach(it -> bodyLines.set(bodyLines.indexOf(it), "~~%s~~".formatted(it)));
            bodyLines.addLast(event.resolve("reverted-at-inline", entry("revertedAt", DATE_TIME_SHORT.format(reverted.revertedAt().getTime()))));
        } else if (revokeAt != null) {
            bodyLines.addFirst(event.resolve("revoke-at-inline", entry("revokeAt", DATE_TIME_SHORT.format(revokeAt.getTime()))));
            bodyLines.addFirst(event.resolve("duration-inline", entry("duration", Helpers.formatDuration(Duration.ofMillis(duration)))));
        }
        return new MessageEmbed.Field(
                headLine,
                String.join("\n", bodyLines),
                false
        );
    }

    public Embed toEmbed(ReplyableEvent<?> event) {
        var embed = event.embed("moderationActDetail");
        embed.placeholders(
                entry("id", id),
                entry("type", event.resolve(type.localizationKey())),
                entry("created", formatTimestamp(createdAt)),
                entry("issuer", Helpers.formatUser(event.getJDA(), issuer)),
                entry("reason", reason),
                entry("color", EmbedColors.DEFAULT));

        paragraph().ifPresent(it -> embed.fields().add(event.resolve("rule"), it.fullDisplay()));
        referenceMessage().ifPresent(it -> embed.fields().add(event.resolve("reference-message"), it.jumpUrl(event.getGuild())));

        if (this instanceof RevertedModerationAct reverted && !reverted.revertedBy().getId().equals(event.getJDA().getSelfUser().getId())) {
            embed.fields()
                    .add(event.resolve("reverted-at"), Helpers.formatTimestamp(reverted.revertedAt()))
                    .add(event.resolve("reverted-by"), Helpers.formatUser(event.getJDA(), reverted.revertedBy()))
                    .add(event.resolve("reverting-reason"), reverted.revertingReason());
        } else if (revokeAt != null) {
            embed.fields()
                    .add(event.resolve("duration-field"), Helpers.formatDuration(Duration.ofMillis(duration)))
                    .add(event.resolve("revoke-at-field"), Helpers.formatTimestamp(revokeAt));
        }
        return embed;
    }

    public RevertedModerationAct revert(Guild guild, Function<String, Embed> embedFunction, User revertedBy, String reason) {
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

        sendRevertMessageToUser(guild, embedFunction, revertedBy, reason);
        return (RevertedModerationAct) ModerationActService.get(id).orElseThrow();
    }

    private void sendRevertMessageToUser(Guild guild, Function<String, Embed> embedFunction, User revertedBy, String reason) {
        var embed = embedFunction.apply("moderationActReverted").placeholders(
                entry("type", type == ModerationActType.TIMEOUT ? "Timeout" : "Warn"),
                entry("date", DEFAULT.format(createdAt.getTime())),
                entry("id", id),
                entry("reason", reason),
                entry("revertedBy", Helpers.formatUser(guild.getJDA(), revertedBy))
        );

        Helpers.sendDM(user, guild.getJDA(), channel -> channel.sendMessageEmbeds(embed.build()));
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

    public long duration() {
        return duration;
    }
}
