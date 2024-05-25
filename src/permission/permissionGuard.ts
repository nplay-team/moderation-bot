import { CommandInteraction, GuildMember } from 'discord.js';
import { GuardFunction } from 'discordx';
import { getUserPermissions, hasPermission } from './permissionHelpers.js';
import { Permission } from './permissions.js';

/**
 * Require a permission to be present in the user's or role's permissions
 * @param permission The permission to check for
 * @returns The guard function for discordx
 */
export const RequirePermission = (
	...permission: Permission[]
): GuardFunction<CommandInteraction> => {
	return async (interaction, _, next) => {
		const member = interaction.member as GuildMember;

		if (member.permissions.has('Administrator')) return next();

		const userPermissions = await getUserPermissions(member);

		// Check if the user has all the required permission
		if (permission.every((p) => hasPermission(userPermissions, p))) {
			return next();
		}

		// TODO: Add embed
		await interaction.reply({
			content: `You do not have the required permission \`${permission}\``,
			ephemeral: true
		});
	};
};
