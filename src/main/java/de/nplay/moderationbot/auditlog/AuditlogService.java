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
import java.util.Objects;
import java.util.Optional;

public class AuditlogService {

    public Optional<AuditlogEntry> get(long id, Guild guild) {
        return Query.query("SELECT * FROM auditlog WHERE id = ?")
                .single(Call.of().bind(id))
                .map(row -> map(row, guild))
                .first();
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
            case SLOWMODE_UPDATE -> guild.getGuildChannelById(targetId);
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
