package de.nplay.moderationbot.moderation.act;

import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.moderation.MessageReferenceService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder.ModerationActCreateData;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ModerationActService {

    public static ModerationAct create(ModerationActCreateData data) {
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

    public static Optional<ModerationAct> get(long moderationId) {
        return Query.query("SELECT * FROM moderations WHERE id = ?")
                .single(Call.of().bind(moderationId))
                .map(ModerationActService::map)
                .first();
    }


    public static Collection<ModerationAct> getToRevert() {
        return Query.query("SELECT * FROM moderations WHERE reverted = false AND revoke_at < ? ORDER BY created_at DESC")
                .single(Call.of().bind(new Timestamp(System.currentTimeMillis())))
                .map(ModerationActService::map)
                .all();
    }

    public static List<ModerationAct> get(UserSnowflake user, int limit, int offset) {
        return Query.query("SELECT * FROM moderations WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?")
                .single(Call.of()
                        .bind(user.getIdLong())
                        .bind(limit)
                        .bind(offset)
                ).map(ModerationActService::map)
                .all();
    }

    public static int count(UserSnowflake user) {
        return Query.query("SELECT COUNT(*) FROM moderations WHERE user_id = ?")
                .single(Call.of().bind(user.getIdLong()))
                .mapAs(Integer.class)
                .first().orElse(0);
    }

    public static void delete(long moderationId) {
        Query.query("DELETE FROM moderations WHERE id = ?")
                .single(Call.of().bind(moderationId))
                .delete();
    }

    public static boolean isTimeOuted(long userId) {
        return Query.query("SELECT EXISTS(SELECT 1 FROM moderations WHERE user_id = ? AND TYPE = 'TIMEOUT' AND reverted = FALSE)")
                .single(Call.of().bind(userId))
                .map(row -> row.getBoolean(1))
                .first().orElse(false);
    }

    public static boolean isBanned(long userId) {
        return Query.query("SELECT EXISTS(SELECT 1 FROM moderations WHERE user_id = ? AND TYPE IN ('BAN', 'TEMP_BAN') AND reverted = FALSE)")
                .single(Call.of().bind(userId))
                .map(row -> row.getBoolean(1))
                .first().orElse(false);
    }

    public static ModerationAct map(Row row) throws SQLException {
        if (row.getBoolean("reverted")) {
            return new RevertedModerationAct(row);
        }
        return new ModerationAct(row);
    }
}
