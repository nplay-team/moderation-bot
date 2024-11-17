package de.nplay.moderationbot.permissions;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for basic CRUD operations on user or role permissions
 *
 */
public class BotPermissionsService {
    /**
     * Mapping of a user permission
     *
     * @param userId      the id of the user
     * @param permissions the bitfield permission value
     * @see BotPermissionBitfield
     */
    public record UserPermissions(long userId, int permissions) {

        /**
         * Mapping method for the {@link de.chojo.sadu.mapper.rowmapper.RowMapper RowMapper}
         *
         * @return a {@link RowMapping} of this record
         */
        @MappingProvider("")
        public static RowMapping<UserPermissions> map() {
            return row -> new UserPermissions(row.getLong("id"), row.getInt(("permissions")));
        }
    }

    /**
     * Mapping of a role permission
     *
     * @param roleId      the id of the role
     * @param permissions the bitfield permission value
     * @see BotPermissionBitfield
     */
    public record RolePermissions(long roleId, int permissions) {

        /**
         * Mapping method for the {@link de.chojo.sadu.mapper.rowmapper.RowMapper RowMapper}
         *
         * @return a {@link RowMapping} of this record
         */
        @MappingProvider("")
        public static RowMapping<RolePermissions> map() {
            return row -> new RolePermissions(row.getLong("id"), row.getInt(("permissions")));
        }
    }

    /**
     * Gets the {@link UserPermissions} of a user. Returns empty permissions if no user entry exists
     *
     * @param user the user
     * @return the {@link UserPermissions}
     */
    @NotNull
    public static UserPermissions getUserPermissions(UserSnowflake user) {
        return Query.query("SELECT * FROM users WHERE id = ?")
                .single(Call.of().bind(user.getIdLong()))
                .mapAs(UserPermissions.class)
                .first().orElse(new UserPermissions(user.getIdLong(), 0));
    }

    /**
     * Gets the {@link UserPermissions} of a member. Returns empty permissions if no user entry exists
     *
     * @param member the member to get the permissions for
     * @return the {@link UserPermissions}
     */
    @NotNull
    public static UserPermissions getMemberPermissions(Member member) {
        var userPermission = Query.query("SELECT * FROM users WHERE id = ?")
                .single(Call.of().bind(member.getIdLong()))
                .mapAs(UserPermissions.class)
                .first().orElse(new UserPermissions(member.getIdLong(), 0));

        int permission = userPermission.permissions;

        for (Role role : member.getRoles()) {
            permission = BotPermissions.combine(permission, getRolePermissions(role.getIdLong()).permissions);
        }

        return new UserPermissions(member.getIdLong(), permission);
    }

    /**
     * Inserts {@link UserPermissions} to the database
     *
     * @param userId      the id of the user
     * @param permissions the bitfield permission value
     */
    public static void createUserPermissions(long userId, int permissions) {
        Query.query("INSERT INTO users(id, permissions) VALUES(? ,?) ON CONFLICT DO NOTHING")
                .single(Call.of().bind(userId).bind(permissions))
                .insert();
    }

    /**
     * Updates the permissions of a user
     *
     * @param userId      the id of the user
     * @param permissions the bitfield permission value
     */
    public static void updateUserPermissions(long userId, int permissions) {
        Query.query("UPDATE users SET permissions = ? WHERE user_id = ?")
                .single(Call.of().bind(permissions).bind(userId))
                .update();
    }

    /**
     * Deletes a {@link UserPermissions} entry from the database. This has the same effect as setting the bitfield
     * permission value to {@code 0}
     *
     * @param userId the id of the user
     */
    public static void deleteUserPermissions(long userId) {
        Query.query("DELETE FROM users WHERE user_id = ?")
                .single(Call.of().bind(userId))
                .delete();
    }

    /**
     * Gets the {@link RolePermissions} of a role. Returns empty permissions if no role entry exists
     *
     * @param roleId the id of the role
     * @return the {@link RolePermissions}
     */
    @NotNull
    public static RolePermissions getRolePermissions(long roleId) {
        return Query.query("SELECT * FROM roles WHERE id = ?")
                .single(Call.of().bind(roleId))
                .mapAs(RolePermissions.class)
                .first().orElse(new RolePermissions(roleId, 0));
    }

    /**
     * Inserts {@link RolePermissions} to the database
     *
     * @param roleId      the id of the role
     * @param permissions the bitfield permission value
     */
    public static void createRolePermissions(long roleId, int permissions) {
        Query.query("INSERT INTO roles(id, permissions) VALUES(? ,?) ON CONFLICT DO NOTHING")
                .single(Call.of().bind(roleId).bind(permissions))
                .insert();
    }

    /**
     * Updates the permissions of a role.
     *
     * @param roleId      the id of the role
     * @param permissions the bitfield permission value
     */
    public static void updateRolePermissions(long roleId, int permissions) {
        Query.query("UPDATE roles SET permissions = ? WHERE role_id = ?")
                .single(Call.of().bind(permissions).bind(roleId))
                .update();
    }

    /**
     * Deletes a {@link RolePermissions} entry from the database. This has the same effect as setting the bitfield
     * permission value to {@code 0}.
     *
     * @param roleId the id of the role
     */
    public static void deleteRolePermissions(long roleId) {
        Query.query("DELETE FROM roles WHERE role_id = ?")
                .single(Call.of().bind(roleId))
                .delete();
    }

    public static boolean hasUserPermissions(UserSnowflake user, BotPermissionBitfield permission) {
        return BotPermissions.hasPermission(getUserPermissions(user).permissions, permission);
    }
}
