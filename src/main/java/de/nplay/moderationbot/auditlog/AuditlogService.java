package de.nplay.moderationbot.auditlog;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class AuditlogService {

    public Optional<AuditlogEntry> get(long id) {
        return Query.query("SELECT * FROM auditlog WHERE id = ?")
                .single(Call.of().bind(id))
                .mapAs(AuditlogEntry.class)
                .first();
    }

    public AuditlogEntry create(AuditlogCreateData data) {
        Long id = Query.query("""
                INSERT INTO auditlog
                (type, issuer_id, target_id, payload)
                VALUES (?::auditlog_type, ?, ?, ?::jsonb)
                """
        ).single(Call.of()
                .bind(data.type())
                .bind(data.issuer().getIdLong())
                .bind(data.target().getIdLong())
                .bind(data.payloadJson().orElse(null))
        ).insertAndGetKeys().keys().getFirst();

        return get(id).orElseThrow();
    }

    public record AuditlogCreateData(
            AuditlogType type,
            UserSnowflake issuer,
            ISnowflake target,
            @Nullable AuditlogPayload payload
    ) {
        public Optional<String> payloadJson() {
            return toJson(payload);
        }

        private Optional<String> toJson(@Nullable AuditlogPayload payload) {
            return Optional.ofNullable(payload).flatMap(AuditlogPayload::toJson);
        }
    }

    public record AuditlogEntry(
            long id,
            AuditlogType type,
            Timestamp createdAt,
            UserSnowflake issuer,
            long target,
            Optional<AuditlogPayload> payload) {

        @MappingProvider("")
        public AuditlogEntry(Row row) throws SQLException {
            AuditlogType type = row.getEnum("type", AuditlogType.class);
            this(
                    row.getLong("id"),
                    type,
                    row.getTimestamp("created_at"),
                    UserSnowflake.fromId(row.getLong("issuer_id")),
                    row.getLong("target_id"),
                    AuditlogPayload.fromJson(type, row.getString("payload"))
            );
        }
    }
}
