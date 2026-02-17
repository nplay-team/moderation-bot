package de.nplay.moderationbot.auditlog;

import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AuditlogService {

    public Optional<AuditlogEntry> get(long id, Guild guild) {
        return Query.query("SELECT * FROM auditlog WHERE id = ?")
                .single(Call.of().bind(id))
                .map(row -> map(row, guild))
                .first();
    }

    public List<AuditlogEntry> getIssuer(UserSnowflake issuer, @Nullable AuditlogType type, int limit, int offset, Guild guild) {
        return getAll("issuer_id", issuer, type, limit, offset, guild);
    }


    public List<AuditlogEntry> getTarget(UserSnowflake target, @Nullable AuditlogType type, int limit, int offset, Guild guild) {
        return getAll("target_id", target, type, limit, offset, guild);
    }

    private List<AuditlogEntry> getAll(String target, UserSnowflake user, @Nullable AuditlogType type, int limit, int offset, Guild guild) {
        Call call = Call.of().bind("id", user.getIdLong())
                .bind("limit", limit)
                .bind("offset", offset);
        if (type != null) {
            call = call.bind("type", type);
        }
        return Query.query("""
                                SELECT * FROM auditlog
                                WHERE %s = :id %s
                                ORDER BY created_at DESC LIMIT :limit OFFSET :offset
                                """,
                        target,
                        type == null ? "" : "AND type = :type::AUDITLOG_TYPE"
                ).single(call)
                .map(row -> map(row, guild))
                .all();
    }

    private List<AuditlogEntry> getAll(AuditlogType type, int limit, int offset, Guild guild) {
        return Query.query("""
                                SELECT * FROM auditlog
                                WHERE type = :type::AUDITLOG_TYPE
                                ORDER BY created_at DESC LIMIT :limit OFFSET :offset
                                """
                ).single(Call.of()
                        .bind("type", type)
                        .bind("limit", limit)
                        .bind("offset", offset)
                ).map(row -> map(row, guild))
                .all();
    }

    public void create(AuditlogCreateData data) {
        Query.query("""
                INSERT INTO auditlog
                (type, issuer_id, target_id, payload)
                VALUES (?::auditlog_type, ?, ?, ?::jsonb)
                """
        ).single(Call.of()
                .bind(data.type())
                .bind(data.issuer().getIdLong())
                .bind(data.target().getIdLong())
                .bind(data.payloadJson().orElse(null))
        ).insert();
    }

    private AuditlogEntry map(Row row, Guild guild) throws SQLException {
        AuditlogType type = row.getEnum("type", AuditlogType.class);

        long targetId = row.getLong("target_id");
        ISnowflake target = switch (type) {
            case PERMISSIONS_ROLE_UPDATE -> guild.getRoleById(targetId);
            case CONFIG_UPDATE -> null;
            case SLOWMODE_UPDATE, MESSAGE_PURGE -> guild.getGuildChannelById(targetId);
            default -> UserSnowflake.fromId(targetId);
        };

        return new AuditlogEntry(row, type, Objects.requireNonNullElse(target, new UnresolvedSnowflake(targetId)));
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
            AbsoluteTime createdAt,
            UserSnowflake issuer,
            ISnowflake target,
            Optional<AuditlogPayload> payload
    ) {

        public AuditlogEntry(Row row, AuditlogType type, ISnowflake target) throws SQLException {
            this(
                    row.getLong("id"),
                    type,
                    new AbsoluteTime(row.getTimestamp("created_at")),
                    UserSnowflake.fromId(row.getLong("issuer_id")),
                    target,
                    AuditlogPayload.fromJson(type, row.getBinaryStream("payload"))
            );
        }
    }

    public record UnresolvedSnowflake(long getIdLong) implements ISnowflake { }

}
