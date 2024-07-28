import { CommandInteraction, GuildMember, PermissionsBitField } from 'discord.js';
import { GuardFunction } from 'discordx';
import { CommandOnlyOnGuildError } from '@/embed/data/genericEmbeds.js';
import { NoPermissionEmbed } from '@/embed/data/permissionEmbeds.js';
import { createEmbed } from '@/embed/embed.js';
import { getUserPermissions, hasPermission } from './permission.helper.js';
import { Permission, PermissionBitmapFlags } from './permission.types.js';

/**
 * Require a permission to be present in the user's or role's permissions
 * @param permissions The permission to check for
 * @returns The guard function for discordx
 */
export const RequirePermission = (
	...permissions: Permission[]
): GuardFunction<CommandInteraction> => {
	return async (interaction, _, next) => {
		const member = interaction.member as GuildMember;

		if (member.permissions.has(PermissionsBitField.Flags.Administrator)) return next();

		const userPermissions = await getUserPermissions(member);

		if (hasPermission(userPermissions, PermissionBitmapFlags.Administrator)) return next();

		// Check if the user has all the required permission
		if (permissions.every((p) => hasPermission(userPermissions, p))) {
			return next();
		}

		await interaction.reply({
			ephemeral: true,
			embeds: [createEmbed(NoPermissionEmbed(permissions))]
		});
	};
};

/**
 * Require the command to be executed in a guild
 * @returns The guard function for discordx
 */
export const OnlyOnGuild: GuardFunction<CommandInteraction> = async (interaction, _, next) => {
	if (interaction.guildId) return next();

	await interaction.reply({
		ephemeral: true,
		embeds: [createEmbed(CommandOnlyOnGuildError())]
	});
};
