package de.nplay.moderationbot.permissions;

import com.github.kaktushose.jda.commands.dispatching.context.InvocationContext;
import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

/// Utility methods for basic CRUD operations on user or role permissions
public class BotPermissionsService {

    public record Permissions(int permissions) {

        @MappingProvider("")
        public static RowMapping<Permissions> map() {
            return row -> new Permissions(row.getInt(("permissions")));
        }

        public boolean hasPermissions(@NotNull InvocationContext<?> context) {
            if ((permissions() & BotPermissions.BitFields.ADMINISTRATOR.value) != 0) {
                return true;
            }
            return context.definition().permissions().stream()
                    .map(BotPermissions.BitFields::valueOf)
                    .noneMatch(it -> (permissions() & it.value) == 0);
        }

        /// Gets a human-readable, line-by-line overview of all included permissions of a bitfield permission value
        @NotNull
        public String readableList() {
            return String.join("\n", BotPermissions.decode(permissions).stream().map(it -> it.displayName).toList());
        }
    }

    /// Gets the [Permissions] of a [UserSnowflake].
    ///
    /// @implNote Returns empty permissions if no user entry exists. If the provided [UserSnowflake] is a [Member] will
    /// combine the [Role] permissions.
    @NotNull
    public static Permissions getUserPermissions(@NotNull UserSnowflake user) {
        var userPermissions = Query.query("SELECT * FROM users WHERE id = ?")
                .single(Call.of().bind(user.getIdLong()))
                .mapAs(Permissions.class)
                .first().orElse(new Permissions(0));

        if (user instanceof Member member) {
            var rolePermissions = 0;
            for (Role role : member.getRoles()) {
                rolePermissions |= getRolePermissions(role).permissions;
            }
            return new Permissions(userPermissions.permissions | rolePermissions);
        }
        return userPermissions;
    }

    /// Sets the permissions for a user. If the user does not have existing permissions,
    /// it creates a new entry. Otherwise, it updates the existing permissions.
    ///
    /// @return the updated [Permissions]
    @NotNull
    public static Permissions updateUserPermissions(@NotNull UserSnowflake user, int permissions) {
        var userPermissions = Query.query("SELECT * FROM users WHERE id = ?")
                .single(Call.of().bind(user.getIdLong()))
                .mapAs(Permissions.class)
                .first();

        if (userPermissions.isEmpty()) {
            Query.query("INSERT INTO users(id, permissions) VALUES(? ,?) ON CONFLICT DO NOTHING")
                    .single(Call.of().bind(user.getIdLong()).bind(permissions))
                    .insert();
        } else {
            Query.query("UPDATE users SET permissions = ? WHERE id = ?")
                    .single(Call.of().bind(permissions).bind(user.getIdLong()))
                    .update();
        }

        return getUserPermissions(user);
    }

    /// Gets the permissions of a role. Returns empty permissions if no role entry exists
    @NotNull
    public static Permissions getRolePermissions(@NotNull Role role) {
        return Query.query("SELECT * FROM roles WHERE id = ?")
                .single(Call.of().bind(role.getIdLong()))
                .mapAs(Permissions.class)
                .first().orElse(new Permissions(0));
    }

    /// Sets the permissions for a role. If the role does not have existing permissions,
    /// it creates a new entry. Otherwise, it updates the existing permissions.
    ///
    /// @return the updated [Permissions]
    @NotNull
    public static Permissions updateRolePermissions(@NotNull Role role, int permissions) {
        var id = role.getIdLong();
        var rolePermissions = Query.query("SELECT * FROM roles WHERE id = ?")
                .single(Call.of().bind(id))
                .mapAs(Permissions.class)
                .first();

        if (rolePermissions.isEmpty()) {
            Query.query("INSERT INTO roles(id, permissions) VALUES(? ,?) ON CONFLICT DO NOTHING")
                    .single(Call.of().bind(id).bind(permissions))
                    .insert();
        } else {
            Query.query("UPDATE roles SET permissions = ? WHERE id = ?")
                    .single(Call.of().bind(permissions).bind(id))
                    .update();
        }

        return getRolePermissions(role);
    }
}
