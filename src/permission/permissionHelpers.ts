import { GuildMember, Role, User } from 'discord.js';
import { NPLAYModerationBot } from '../bot.js';
import { Permission, PermissionBitmap } from './permissions.js';

/**
 * Get the permission bitfield of a user
 * @param guildMember The guild member to get the permissions of
 * @returns The permission bitfield of the user
 */
export async function getUserPermissions(guildMember: GuildMember): Promise<number> {
	const permissions = [
		...(await getPermissionsOfUserRoles(guildMember)),
		...(await getPermissionsOfUser(guildMember))
	];

	return permissions.reduce((acc, p) => acc | p, 0);
}

/**
 * Get the permission bitfield of a mentionable (guild member or role)
 * @param mentionable The mentionable to get the permissions of
 * @param extendSearch Whether to search for permissions in roles of the mentionable
 * @returns The permission bitfield of the mentionable
 */
export async function getMentionablePermissions(
	mentionable: GuildMember | User | Role,
	extendSearch: boolean = true
) {
	if (mentionable instanceof User) return null;
	return mentionable instanceof GuildMember && extendSearch
		? getUserPermissions(mentionable)
		: getPermissions(mentionable.id, mentionable.guild.id);
}

/**
 * Get the permission bitfield by snowflake id
 * @param snowflake The snowflake id to get the permissions of
 * @param guildId The guild id where the snowflake is located
 */
export async function getPermissions(snowflake: string, guildId: string): Promise<number> {
	const permissions = await NPLAYModerationBot.db.permission.findMany({
		where: {
			snowflake: snowflake,
			guildId: guildId
		}
	});

	return permissions.reduce((acc, p) => acc | p.permission, 0);
}

/**
 * Checks if a user has a permission
 * @param current The current permission bitfield
 * @param permission The permission to check
 * @returns Whether the specified permission is present in the bitfield
 */
export function hasPermission(current: number, permission: Permission) {
	return (current & PermissionBitmap[permission]) != 0;
}

/**
 * Adds a permission
 * @param current The current permission bitfield
 * @param permission The permission to add
 */
export function addPermission(current: number, permission: Permission) {
	return current | PermissionBitmap[permission];
}

/**
 * Removes a permission
 * @param current The current permission bitfield
 * @param permission The permission to remove
 */
export function removePermission(current: number, permission: Permission) {
	return current & ~PermissionBitmap[permission];
}

/**
 * Turns a permission bitfield into a readable string
 * @param permissions The permission bitfield to turn into a string
 * @returns The string representation of the permission bitfield
 * @example
 * permissionToString(5) // => 'REPORT_READ, REPORT_DELETE'
 */
export function permissionsToString(permissions: number) {
	return permissions == 0
		? 'Keine'
		: Object.keys(PermissionBitmap)
				.filter((p) => hasPermission(permissions, p as Permission))
				.join(', ');
}

/**
 * Decodes a permission bitfield into an array of permissions
 * @param permissions The permission bitfield to decode
 * @returns An array of permissions
 */
export function decodePermissions(permissions: number) {
	return Object.keys(PermissionBitmap).filter((p) => hasPermission(permissions, p as Permission));
}

/**
 * Updates the permissions of a user or role in the database
 * @param snowflake The snowflake id of the user or role
 * @param guildId The guild id where the snowflake is located
 * @param permissions The new permission bitfield
 * @returns A promise that resolves when the permissions have been updated
 */
export function updatePermissions(snowflake: string, guildId: string, permissions: number) {
	return NPLAYModerationBot.db.permission.upsert({
		where: {
			id: {
				snowflake: snowflake,
				guildId: guildId
			}
		},
		create: {
			snowflake: snowflake,
			guildId: guildId,
			permission: permissions
		},
		update: {
			permission: permissions
		}
	});
}

// private functions
async function getPermissionsOfUserRoles(guildMember: GuildMember) {
	return NPLAYModerationBot.db.permission
		.findMany({
			where: {
				snowflake: {
					in: guildMember.roles.cache.map((role) => role.id)
				},
				guildId: guildMember.guild.id
			}
		})
		.then((permissions) => permissions.map((p) => p.permission));
}

async function getPermissionsOfUser(guildMember: GuildMember) {
	return NPLAYModerationBot.db.permission
		.findMany({
			where: {
				snowflake: guildMember.id,
				guildId: guildMember.guild.id
			}
		})
		.then((permissions) => permissions.map((p) => p.permission));
}
