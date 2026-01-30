package de.nplay.moderationbot.auditlog;

import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.auditlog.model.AuditlogState;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class AuditlogService {

    private final ModerationActService actService;

    public AuditlogService(ModerationActService actService) {
        this.actService = actService;
    }

    public Optional<AuditlogEntry> get(long id) {
        return Query.query("SELECT * FROM auditlog WHERE id = ?")
                .single(Call.of().bind(id))
                .map(this::map)
                .first();
    }

    public AuditlogEntry create(AuditlogCreateData data) {
        Long id = Query.query("""
                INSERT INTO auditlog
                (type, issuer_id, target_id, moderation_id, old_state, new_state)
                VALUES (?::auditlog_type, ?, ?, ?, ?::json, ?::json)
                """
        ).single(Call.of()
                .bind(data.type())
                .bind(data.issuer().getIdLong())
                .bind(data.target().getIdLong())
                .bind(Optional.ofNullable(data.act()).map(ModerationAct::id).orElse(null))
                .bind(data.oldStateJson().orElse(null))
                .bind(data.newStateJson().orElse(null))
        ).insertAndGetKeys().keys().getFirst();

        return get(id).orElseThrow();
    }

    public record AuditlogCreateData(
            AuditlogType type,
            UserSnowflake issuer,
            ISnowflake target,
            @Nullable ModerationAct act,
            @Nullable AuditlogState oldState,
            @Nullable AuditlogState newState
    ) {
        public Optional<String> oldStateJson() {
            return toJson(oldState);
        }

        public Optional<String> newStateJson() {
            return toJson(newState);
        }

        private Optional<String> toJson(@Nullable AuditlogState state) {
            return Optional.ofNullable(state).flatMap(AuditlogState::toJson);
        }
    }

    private AuditlogEntry map(Row row) throws SQLException {
        return new AuditlogEntry(row, actService.get(row.getLong("moderation_id")).orElse(null));
    }

    public record AuditlogEntry(
            long id,
            AuditlogType type,
            Timestamp createdAt,
            UserSnowflake issuer,
            long target,
            Optional<ModerationAct> act,
            Optional<AuditlogState> oldState,
            Optional<AuditlogState> newState
    ) {
        public AuditlogEntry(Row row, @Nullable ModerationAct act) throws SQLException {
            AuditlogType type = row.getEnum("type", AuditlogType.class);
            this(
                    row.getLong("id"),
                    type,
                    row.getTimestamp("created_at"),
                    UserSnowflake.fromId(row.getLong("issuer_id")),
                    row.getLong("target_id"),
                    Optional.ofNullable(act),
                    AuditlogState.fromJson(type, row.getString("old_state")),
                    AuditlogState.fromJson(type, row.getString("new_state"))
            );
        }
    }
}
