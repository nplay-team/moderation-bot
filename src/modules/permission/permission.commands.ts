import {
	ApplicationCommandOptionType,
	CommandInteraction,
	GuildMember,
	PermissionsBitField,
	Role,
	StringSelectMenuInteraction,
	User
} from 'discord.js';
import { Discord, Guard, SelectMenuComponent, Slash, SlashGroup, SlashOption } from 'discordx';
import {
	PermissionGetEmbed,
	PermissionListEmbed,
	PermissionManageEmbed
} from '@/embed/data/permissionEmbeds.js';
import { createEmbed } from '@/embed/embed.js';
import { permissionMenu } from './permission.components.js';
import { RequirePermission } from './permission.guards.js';
import { getMentionablePermissions } from './permission.helper.js';
import { editPermissions } from './permission.service.js';
import { PermissionBitmap, PermissionBitmapFlags } from './permission.types.js';

@Discord()
@SlashGroup({
	name: 'permissions',
	description: 'Alle Befehle zum Verwalten von Berechtigungen',
	dmPermission: false,
	defaultMemberPermissions: [PermissionsBitField.Flags.BanMembers]
})
@SlashGroup('permissions')
export abstract class PermissionCommands {
	private static editPermissionId: string | null = null;
	private static editPermissionMentionable: GuildMember | User | Role | null = null;

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
		const permissions = await getMentionablePermissions(mentionable);

		await interaction.reply({
			ephemeral: true,
			embeds: [createEmbed(PermissionGetEmbed(permissions || 0, mentionable))]
		});
	}

	@Slash({
		name: 'manage',
		description: 'Verwaltet die Berechtigungen eines Benutzers oder einer Rolle'
	})
	@Guard(RequirePermission(PermissionBitmapFlags.PermissionManage))
	async managePermissions(
		@SlashOption({
			name: 'mentionable',
			description: 'Der Benutzer/Die Rolle dessen Berechtigungen bearbeitet werden soll',
			required: true,
			type: ApplicationCommandOptionType.Mentionable
		})
		mentionable: GuildMember | User | Role,

		interaction: CommandInteraction
	) {
		PermissionCommands.editPermissionId = interaction.id;
		PermissionCommands.editPermissionMentionable = mentionable;

		await interaction.reply({
			components: [permissionMenu(await getMentionablePermissions(mentionable, false))],
			embeds: [createEmbed(PermissionManageEmbed(mentionable))]
		});
	}

	@SelectMenuComponent({ id: 'edit-permissions' })
	@Guard(RequirePermission(PermissionBitmapFlags.PermissionManage))
	async editPermissions(interaction: StringSelectMenuInteraction) {
		await interaction.deferReply();
		await editPermissions(
			interaction,
			PermissionCommands.editPermissionId,
			PermissionCommands.editPermissionMentionable
		);
	}
}
