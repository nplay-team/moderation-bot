package de.nplay.moderationbot.permissions;

import java.util.Set;

public class BotPermissions {
    
    /**
     * Combines multiple permissions into one integer.
     * @param permissions the list of integer to combine
     * @return the combined permissions
     */
    public static int combine(int... permissions) {
        int combined = 0;
        for (int permission : permissions) {
            combined |= permission;
        }
        return combined;
    }
    
    /**
     * Combines multiple permissions into one integer.
     * @param permissions the list of {@link BotPermissionBitfield} to combine
     * @return the combined permissions
     */
    public static int combine(BotPermissionBitfield... permissions) {
        int combined = 0;
        for (BotPermissionBitfield permission : permissions) {
            combined |= permission.value;
        }
        return combined;
    }

    /**
     * Adds one permission to a permission integer.
     * @param permissions the current bitfield permission value
     * @param permission the {@link BotPermissionBitfield} to add
     * @return the new permissions
     */
    public static int addPermission(int permissions, BotPermissionBitfield permission) {
        return permissions | permission.value;
    }

    /**
     * Adds multiple permissions to a permission integer.
     * @param permissions the current bitfield permission value
     * @param permissionsToAdd the list of {@link BotPermissionBitfield} to add
     * @return the new permissions
     */
    public static int addPermission(int permissions, BotPermissionBitfield... permissionsToAdd) {
        int newPermissions = permissions;
        for (BotPermissionBitfield permission : permissionsToAdd) {
            newPermissions = addPermission(newPermissions, permission);
        }
        return newPermissions;
    }

    /**
     * Removes one permission from a permission integer.
     * @param permissions the current bitfield permission value
     * @param permission the {@link BotPermissionBitfield} to add
     * @return the new permissions
     */
    public static int removePermission(int permissions, BotPermissionBitfield permission) {
        return permissions & ~permission.value;
    }

    /**
     * Removes multiple permissions from a permission integer.
     * @param permissions the current bitfield permission value
     * @param permissionsToRemove the list of {@link BotPermissionBitfield} to remove
     * @return the new permissions
     */
    public static int removePermission(int permissions, BotPermissionBitfield... permissionsToRemove) {
        int newPermissions = permissions;
        for (BotPermissionBitfield permission : permissionsToRemove) {
            newPermissions = removePermission(newPermissions, permission);
        }
        return newPermissions;
    }

    /**
     * Checks whether a specific permission is present in the provided permission integer. 
     * @param permissions the current bitfield permission value
     * @param permission the {@link BotPermissionBitfield} to check against
     * @return Whether the permission is present or not
     */
    public static boolean hasPermission(int permissions, BotPermissionBitfield permission) {
        return (permissions & permission.value) != 0;
    }
    
    /**
     * Checks whether multiple permissions are present in the provided permission integer.
     * @param permissions the current bitfield permission value
     * @param permissionsToCheck a list of {@link BotPermissionBitfield} to check against
     * @return Whether all permissions are present or not
     */
    public static boolean hasPermission(int permissions, BotPermissionBitfield... permissionsToCheck) {
        for (BotPermissionBitfield permission : permissionsToCheck) {
            if (!hasPermission(permissions, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether multiple permissions are present in the provided permission integer.
     * @param permissions the current bitfield permission value
     * @param permissionNames a list of permission names to check against
     * @return Whether all permissions are present or not
     */
    public static boolean hasPermission(int permissions, Set<String> permissionNames) {
        for (String permissionName : permissionNames) {
            BotPermissionBitfield permission = BotPermissionBitfield.valueOf(permissionName);
            if (!hasPermission(permissions, permission)) {
                return false;
            }
        }
        return true;
    }
}
