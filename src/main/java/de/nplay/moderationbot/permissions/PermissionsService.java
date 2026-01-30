package de.nplay.moderationbot.permissions;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.auditlog.lifecycle.Lifecycle;
import de.nplay.moderationbot.auditlog.lifecycle.Service;
import de.nplay.moderationbot.auditlog.lifecycle.events.PermissionsEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.permissions.BotPermissions.BitFields;
import io.github.kaktushose.jdac.dispatching.context.InvocationContext;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.sql.SQLException;

public class PermissionsService extends Service {

    public PermissionsService(Lifecycle lifecycle) {
        super(lifecycle);
    }

    public EntityPermissions getUser(UserSnowflake user) {
        return Query.query("SELECT * FROM users WHERE id = ?")
                .single(Call.of().bind(user.getIdLong()))
                .mapAs(EntityPermissions.class)
                .first().orElse(new EntityPermissions(0));
    }

    public EntityPermissions getRole(Role role) {
        return Query.query("SELECT * FROM roles WHERE id = ?")
                .single(Call.of().bind(role.getIdLong()))
                .mapAs(EntityPermissions.class)
                .first().orElse(new EntityPermissions(0));
    }

    public EntityPermissions getCombined(Member member) {
        int rolePermissions = member.getRoles().stream()
                .map(this::getRole)
                .mapToInt(EntityPermissions::permissions)
                .reduce(0, ((left, right) -> left | right));
        return new EntityPermissions(getUser(member).permissions | rolePermissions);
    }

    public void updateUser(UserSnowflake target, int permissions, UserSnowflake issuer) {
        var userPermissions = Query.query("SELECT * FROM users WHERE id = ?")
                .single(Call.of().bind(target.getIdLong()))
                .mapAs(EntityPermissions.class)
                .first();

        publish(new PermissionsEvent(
                AuditlogType.PERMISSIONS_USER_UPDATE,
                issuer,
                target,
                userPermissions.map(EntityPermissions::permissions).orElse(0),
                permissions
        ));

        if (userPermissions.isEmpty()) {
            Query.query("INSERT INTO users(id, permissions) VALUES(? ,?) ON CONFLICT DO NOTHING")
                    .single(Call.of().bind(target.getIdLong()).bind(permissions))
                    .insert();
        } else {
            Query.query("UPDATE users SET permissions = ? WHERE id = ?")
                    .single(Call.of().bind(permissions).bind(target.getIdLong()))
                    .update();
        }
    }

    public EntityPermissions updateRole(Role role, int permissions, UserSnowflake issuer) {
        var id = role.getIdLong();
        var rolePermissions = Query.query("SELECT * FROM roles WHERE id = ?")
                .single(Call.of().bind(id))
                .mapAs(EntityPermissions.class)
                .first();

        publish(new PermissionsEvent(
                AuditlogType.PERMISSIONS_ROLE_UPDATE,
                issuer,
                role,
                rolePermissions.map(EntityPermissions::permissions).orElse(0),
                permissions
        ));

        if (rolePermissions.isEmpty()) {
            Query.query("INSERT INTO roles(id, permissions) VALUES(? ,?) ON CONFLICT DO NOTHING")
                    .single(Call.of().bind(id).bind(permissions))
                    .insert();
        } else {
            Query.query("UPDATE roles SET permissions = ? WHERE id = ?")
                    .single(Call.of().bind(permissions).bind(id))
                    .update();
        }

        return getRole(role);
    }

    public record EntityPermissions(int permissions) {

        @MappingProvider("")
        public EntityPermissions(Row row) throws SQLException {
            this(row.getInt("permissions"));
        }

        public boolean hasPermissions(InvocationContext<?> context) {
            if ((permissions() & BitFields.ADMINISTRATOR.value) != 0) {
                return true;
            }
            return context.definition().permissions().stream()
                    .map(BitFields::valueOf)
                    .noneMatch(it -> (permissions() & it.value) == 0);
        }

        public boolean hasPermission(String permission) {
            return (permissions() & BitFields.valueOf(permission).value) != 0;
        }

        public String readableList(ReplyableEvent<?> event) {
            if (permissions == 0) {
                return event.resolve("- %s".formatted(event.resolve("perm-none")));
            }
            return String.join("\n", BotPermissions.decode(permissions).stream()
                    .map(it -> "- %s".formatted(event.resolve(it.localizationKey)))
                    .toList());
        }
    }
}
