package de.nplay.moderationbot.moderation.act;

import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.lifecycle.Lifecycle;
import de.nplay.moderationbot.auditlog.lifecycle.Service;
import de.nplay.moderationbot.auditlog.lifecycle.events.ModerationEvent;
import de.nplay.moderationbot.moderation.MessageReferenceService;
import de.nplay.moderationbot.moderation.MessageReferenceService.MessageReference;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder.ModerationActCreateData;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.message.resolver.Resolver;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public class ModerationActService extends Service {

    private final MessageReferenceService referenceService;
    private final RuleService ruleService;

    public ModerationActService(MessageReferenceService referenceService, RuleService ruleService, Lifecycle lifecycle) {
        super(lifecycle);
        this.referenceService = referenceService;
        this.ruleService = ruleService;
    }

    public ModerationAct create(ModerationActCreateData data) {
        data.messageReference().ifPresent(referenceService::create);

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
                .bind(data.ruleParagraph().map(RuleParagraph::id).orElse(null))
                .bind(data.messageReference().map(ISnowflake::getIdLong).orElse(null))
                .bind(data.revokeAt().orElse(null))
                .bind(Optional.ofNullable(data.duration()).map(Duration::toMillis).orElse(0L))
                .bind(new Timestamp(System.currentTimeMillis()))
        ).insertAndGetKeys().keys().getFirst();

        ModerationAct act = get(id).orElseThrow();
        publish(new ModerationEvent.Create(act));
        return act;
    }

    public Optional<ModerationAct> get(long moderationId) {
        return Query.query("SELECT * FROM moderations WHERE id = ?")
                .single(Call.of().bind(moderationId))
                .map(this::map)
                .first();
    }

    public List<ModerationAct> get(UserSnowflake user, int limit, int offset) {
        return Query.query("SELECT * FROM moderations WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?")
                .single(Call.of()
                        .bind(user.getIdLong())
                        .bind(limit)
                        .bind(offset)
                ).map(this::map)
                .all();
    }

    public Collection<ModerationAct> getToRevert() {
        return Query.query("SELECT * FROM moderations WHERE reverted = false AND revoke_at < ? ORDER BY created_at DESC")
                .single(Call.of().bind(new Timestamp(System.currentTimeMillis())))
                .map(this::map)
                .all();
    }

    public int count(UserSnowflake user) {
        return Query.query("SELECT COUNT(*) FROM moderations WHERE user_id = ?")
                .single(Call.of().bind(user.getIdLong()))
                .mapAs(Integer.class)
                .first().orElse(0);
    }

    public void delete(long moderationId) {
        Query.query("DELETE FROM moderations WHERE id = ?")
                .single(Call.of().bind(moderationId))
                .delete();
    }

    public boolean isTimeOuted(UserSnowflake user) {
        return Query.query("SELECT EXISTS(SELECT 1 FROM moderations WHERE user_id = ? AND TYPE = 'TIMEOUT' AND reverted = FALSE)")
                .single(Call.of().bind(user.getIdLong()))
                .map(row -> row.getBoolean(1))
                .first().orElse(false);
    }

    public boolean isBanned(UserSnowflake user) {
        return Query.query("SELECT EXISTS(SELECT 1 FROM moderations WHERE user_id = ? AND TYPE IN ('BAN', 'TEMP_BAN') AND reverted = FALSE)")
                .single(Call.of().bind(user.getIdLong()))
                .map(row -> row.getBoolean(1))
                .first().orElse(false);
    }

    public RevertedModerationAct revert(ModerationAct act, ReplyableEvent<?> event, String reason) {
        return revert(act, event.getGuild(), event.getUser(), reason, event.getUserLocale());
    }

    public void automaticRevert(Guild guild, Resolver<String> resolver) {
        getToRevert().forEach(act -> {
            RevertedModerationAct reverted = revert(
                    act,
                    guild,
                    guild.getJDA().getSelfUser(),
                    resolver.resolve("automatic-revert-reason", DiscordLocale.GERMAN),
                    DiscordLocale.GERMAN
            );
            publish(new ModerationEvent.Revert(reverted, true));
        });
    }

    private RevertedModerationAct revert(ModerationAct act, Guild guild, User revertedBy, String reason, DiscordLocale locale) {
        if (act instanceof RevertedModerationAct reverted) {
            return reverted;
        }

        Query.query("UPDATE moderations SET reverted = true, reverted_by = ?, reverted_at = ?, revert_reason = ? WHERE id = ?")
             .single(Call.of()
                         .bind(revertedBy.getIdLong())
                         .bind(new Timestamp(System.currentTimeMillis()))
                         .bind(reason)
                         .bind(act.id()))
             .update();

        switch (act.type()) {
            case BAN, TEMP_BAN -> Helpers.complete(guild.unban(act.user()));
            case TIMEOUT -> Helpers.complete(guild.retrieveMember(act.user()).flatMap(Member::removeTimeout));
        }

        sendRevertMessageToUser(act, guild, revertedBy, reason, locale);

        return (RevertedModerationAct) get(act.id()).orElseThrow();
    }

    private void sendRevertMessageToUser(ModerationAct act, Guild guild, User revertedBy, String reason, DiscordLocale locale) {
        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("revert$revert-info"),
                Separator.createDivider(Separator.Spacing.SMALL),
                entry("type", act.type().localized(locale))
        ).withAccentColor(Replies.SUCCESS);
        container.append(
                TextDisplay.of("revert$revert-info.body"),
                entry("id", act.id()),
                entry("date", act.createdAt()),
                entry("reason", reason)
        );
        container.append(TextDisplay.of("revert$revert-info.reverter"), entry("revertedBy", revertedBy));

        Helpers.sendDM(act.user(), guild.getJDA(), channel -> channel.sendMessageComponents(container).useComponentsV2());
    }

    private ModerationAct map(Row row) throws SQLException {
        MessageReference referenceMessage = referenceService.get(row.getLong("reference_message")).orElse(null);
        RuleParagraph paragraph = ruleService.get(row.getInt("paragraph_id")).orElse(null);
        if (row.getBoolean("reverted")) {
            return new RevertedModerationAct(row, referenceMessage, paragraph);
        }
        return new ModerationAct(row, referenceMessage, paragraph);
    }
}
