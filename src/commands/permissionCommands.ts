import {
	ApplicationCommandOptionType,
	CommandInteraction,
	GuildMember,
	Role,
	User
} from 'discord.js';
import { Discord, Guard, Slash, SlashChoice, SlashGroup, SlashOption } from 'discordx';
import { RequirePermission } from '../permission/permissionGuard.js';
import {
	addPermission,
	getMentionablePermissions,
	permissionsToString,
	removePermission,
	updatePermissions
} from '../permission/permissionHelpers.js';
import {
	Permission,
	PermissionBitmap,
	PermissionBitmapFlags,
	PermissionBitmapSpecials,
	getPermissionDescription
} from '../permission/permissions.js';

@Discord()
@SlashGroup({
	name: 'permissions',
	description: 'Alle Befehle zum Verwalten von Berechtigungen',
	dmPermission: false
})
@SlashGroup('permissions')
abstract class PermissionCommands {
	@Slash({
		name: 'list',
		description: 'Listet alle verfügbaren Berechtigungen auf'
	})
	@Guard(RequirePermission(PermissionBitmapFlags.PermissionRead))
	async listPermissions(interaction: CommandInteraction) {
		await interaction.reply({
			content: `\`${permissionsToString(PermissionBitmapSpecials.ALL)}\``,
			ephemeral: true
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
				content: 'Dieser Befehl kann nur in einem Server verwendet werden',
				ephemeral: true
			});
		}

		const permissions = await getMentionablePermissions(mentionable);

		// TODO: Add embed
		await interaction.reply({
			content: `\`${permissionsToString(permissions || 0)}\``,
			ephemeral: true
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
				content: 'Dieser Befehl kann nur in einem Server verwendet werden',
				ephemeral: true
			});
		}

		await interaction.deferReply({ ephemeral: true });

		const permissions = await getMentionablePermissions(mentionable);

		if (permissions === null) {
			return await interaction.editReply(
				'Diesem Benutzer können keine Berechtigungen hinzugefügt werden'
			);
		}

		const newPermissions = addPermission(permissions, permission);

		// TODO: add modlog entry

		updatePermissions(mentionable.id, interaction.guildId, newPermissions).then(async (perm) => {
			await interaction.editReply(
				`Die Berechtigung \`${permission}\` wurde ${perm ? 'aktualisiert' : 'gewährt'}`
			);
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
				content: 'Dieser Befehl kann nur in einem Server verwendet werden',
				ephemeral: true
			});
		}

		await interaction.deferReply({ ephemeral: true });

		const permissions = await getMentionablePermissions(mentionable);

		if (!permissions) {
			return await interaction.editReply(
				'Diesem Benutzer können keine Berechtigungen entzogen werden'
			);
		}

		const newPermissions = removePermission(permissions, permission);

		// TODO: add modlog entry

		updatePermissions(mentionable.id, interaction.guildId, newPermissions).then(async (perm) => {
			await interaction.editReply(
				`Die Berechtigung \`${permission}\` wurde ${perm ? 'aktualisiert' : 'entzogen'}`
			);
		});
	}

	static getPermissionChoices() {
		return (Object.keys(PermissionBitmap) as Permission[]).map((p) => ({
			name: getPermissionDescription(p),
			value: p
		}));
	}
}
