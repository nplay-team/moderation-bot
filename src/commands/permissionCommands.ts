import {
	ApplicationCommandOptionType,
	CommandInteraction,
	GuildMember,
	Role,
	User
} from 'discord.js';
import { Discord, Guard, Slash, SlashChoice, SlashGroup, SlashOption } from 'discordx';
import { CommandOnlyOnGuildError } from '../embed/data/genericEmbeds.js';
import {
	PermissionGetEmbed,
	PermissionListEmbed,
	PermissionSetEmbed,
	PermissionSetErrorEmbed
} from '../embed/data/permissionEmbeds.js';
import { createEmbed } from '../embed/embed.js';
import { RequirePermission } from '../permission/permissionGuard.js';
import {
	addPermission,
	getMentionablePermissions,
	removePermission,
	updatePermissions
} from '../permission/permissionHelpers.js';
import {
	Permission,
	PermissionBitmap,
	PermissionBitmapFlags,
	getPermissionDescription
} from '../permission/permissions.js';

@Discord()
@SlashGroup({
	name: 'permissions',
	description: 'Alle Befehle zum Verwalten von Berechtigungen',
	dmPermission: false
})
@SlashGroup('permissions')
export abstract class PermissionCommands {
	@Slash({
		name: 'list',
		description: 'Listet alle verfügbaren Berechtigungen auf'
	})
	@Guard(RequirePermission(PermissionBitmapFlags.PermissionRead))
	async listPermissions(interaction: CommandInteraction) {
		await interaction.reply({
			ephemeral: true,
			embeds: [createEmbed(PermissionListEmbed(Object.keys(PermissionBitmap)))]
		});
	}

	@Slash({
		name: 'get',
		description: 'Gibt die Berechtigungen eines Benutzers oder einer Rolle zurück'
	})
	@Guard(RequirePermission(PermissionBitmapFlags.PermissionRead))
	async getPermissions(
		@SlashOption({
			name: 'mentionable',
			description: 'Der Benutzer oder die Rolle, dessen Berechtigungen abgerufen werden sollen',
			required: true,
			type: ApplicationCommandOptionType.Mentionable
		})
		mentionable: GuildMember | User | Role,

		interaction: CommandInteraction
	) {
		if (!interaction.guildId) {
			return await interaction.reply({
				ephemeral: true,
				embeds: [createEmbed(CommandOnlyOnGuildError())]
			});
		}

		const permissions = await getMentionablePermissions(mentionable);

		await interaction.reply({
			ephemeral: true,
			embeds: [createEmbed(PermissionGetEmbed(permissions || 0, mentionable))]
		});
	}

	@Slash({
		name: 'grant',
		description: 'Gewährt einem Benutzer oder einer Rolle eine Berechtigung'
	})
	@Guard(RequirePermission(PermissionBitmapFlags.PermissionGrant))
	async grantPermission(
		@SlashOption({
			name: 'mentionable',
			description: 'Der Benutzer oder die Rolle, dem/welcher die Berechtigung gewährt werden soll',
			required: true,
			type: ApplicationCommandOptionType.Mentionable
		})
		mentionable: GuildMember | User | Role,

		@SlashChoice(...PermissionCommands.getPermissionChoices())
		@SlashOption({
			name: 'permission',
			description: 'Die Berechtigung, die gewährt werden soll',
			required: true,
			type: ApplicationCommandOptionType.String
		})
		permission: Permission,

		interaction: CommandInteraction
	) {
		if (!interaction.guildId) {
			return await interaction.reply({
				ephemeral: true,
				embeds: [createEmbed(CommandOnlyOnGuildError())]
			});
		}

		await interaction.deferReply({ ephemeral: true });

		const permissions = await getMentionablePermissions(mentionable);

		if (permissions === null) {
			return await interaction.editReply({
				embeds: [createEmbed(PermissionSetErrorEmbed())]
			});
		}

		const newPermissions = addPermission(permissions, permission);

		// TODO: add modlog entry

		updatePermissions(mentionable.id, interaction.guildId, newPermissions).then(async (perm) => {
			await interaction.editReply({
				embeds: [createEmbed(PermissionSetEmbed(perm.permission, mentionable))]
			});
		});
	}

	@Slash({
		name: 'revoke',
		description: 'Entzieht einem Benutzer oder einer Rolle eine Berechtigung'
	})
	@Guard(RequirePermission(PermissionBitmapFlags.PermissionRevoke))
	async revokePermission(
		@SlashOption({
			name: 'mentionable',
			description: 'Der Benutzer oder die Rolle, dem/welcher die Berechtigung entzogen werden soll',
			required: true,
			type: ApplicationCommandOptionType.Mentionable
		})
		mentionable: GuildMember | User | Role,

		@SlashChoice(...PermissionCommands.getPermissionChoices())
		@SlashOption({
			name: 'permission',
			description: 'Die Berechtigung, die entzogen werden soll',
			required: true,
			type: ApplicationCommandOptionType.String
		})
		permission: Permission,

		interaction: CommandInteraction
	) {
		if (!interaction.guildId) {
			return await interaction.reply({
				ephemeral: true,
				embeds: [createEmbed(CommandOnlyOnGuildError())]
			});
		}

		await interaction.deferReply({ ephemeral: true });

		const permissions = await getMentionablePermissions(mentionable);

		if (!permissions) {
			return await interaction.editReply({ embeds: [createEmbed(PermissionSetErrorEmbed())] });
		}

		const newPermissions = removePermission(permissions, permission);

		// TODO: add modlog entry

		updatePermissions(mentionable.id, interaction.guildId, newPermissions).then(async (perm) => {
			await interaction.editReply({
				embeds: [createEmbed(PermissionSetEmbed(perm.permission, mentionable))]
			});
		});
	}

	static getPermissionChoices() {
		return (Object.keys(PermissionBitmap) as Permission[]).map((p) => ({
			name: getPermissionDescription(p),
			value: p
		}));
	}
}
