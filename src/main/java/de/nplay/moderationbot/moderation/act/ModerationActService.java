package de.nplay.moderationbot.moderation.act;

import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.moderation.MessageReferenceService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder.ModerationActCreateData;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.message.resolver.Resolver;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public class ModerationActService {

    public ModerationAct create(ModerationActCreateData data) {
        if (data.messageReference() != null) {
            MessageReferenceService.createMessageReference(data.messageReference());
        }

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
                .bind(data.paragraphId())
                .bind(data.messageReferenceId().orElse(null))
                .bind(data.revokeAt().orElse(null))
                .bind(Optional.ofNullable(data.duration()).map(Duration::toMillis).orElse(0L))
                .bind(new Timestamp(System.currentTimeMillis()))
        ).insertAndGetKeys().keys().getFirst();

        return get(id).orElseThrow();
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
        getToRevert().forEach(act -> revert(
                act,
                guild,
                guild.getJDA().getSelfUser(),
                resolver.resolve("automatic-revert-reason", DiscordLocale.GERMAN),
                DiscordLocale.GERMAN
        ));
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
        if (row.getBoolean("reverted")) {
            return new RevertedModerationAct(row);
        }
        return new ModerationAct(row);
    }
}
