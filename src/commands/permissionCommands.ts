import {
	ActionRowBuilder,
	ApplicationCommandOptionType,
	CommandInteraction,
	GuildMember,
	MessageActionRowComponentBuilder,
	PermissionsBitField,
	Role,
	StringSelectMenuBuilder,
	StringSelectMenuInteraction,
	User
} from 'discord.js';
import { Discord, Guard, SelectMenuComponent, Slash, SlashGroup, SlashOption } from 'discordx';
import { ComponentStaleError } from '../embed/data/genericEmbeds.js';
import {
	PermissionGetEmbed,
	PermissionListEmbed,
	PermissionManageEmbed,
	PermissionManageSuccessEmbed
} from '../embed/data/permissionEmbeds.js';
import { createEmbed } from '../embed/embed.js';
import { RequirePermission } from '../permission/permissionGuard.js';
import {
	decodePermissions,
	getMentionablePermissions,
	updatePermissions
} from '../permission/permissionHelpers.js';
import {
	getPermissionDescription,
	Permission,
	PermissionBitmap,
	PermissionBitmapFlags
} from '../permission/permissions.js';

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

		const permissions = await getMentionablePermissions(mentionable, false);
		const permissionsArray = decodePermissions(permissions || 0);

		const options = PermissionCommands.getPermissionChoices(permissionsArray);

		const menu = new StringSelectMenuBuilder()
			.setOptions(options)
			.setCustomId('edit-permissions')
			.setMinValues(0)
			.setMaxValues(options.length <= 25 ? options.length : 25)
			.setPlaceholder('Keine Berechtigungen');

		const buttonRow = new ActionRowBuilder<MessageActionRowComponentBuilder>().addComponents(menu);

		await interaction.reply({
			components: [buttonRow],
			embeds: [createEmbed(PermissionManageEmbed(mentionable))]
		});
	}

	@SelectMenuComponent({ id: 'edit-permissions' })
	@Guard(RequirePermission(PermissionBitmapFlags.PermissionManage))
	async editPermissions(interaction: StringSelectMenuInteraction) {
		await interaction.deferReply();

		if (
			interaction.message.interaction?.id !== PermissionCommands.editPermissionId ||
			!PermissionCommands.editPermissionMentionable
		) {
			return await interaction.message.delete().finally(() => {
				interaction
					.followUp({
						ephemeral: true,
						embeds: [createEmbed(ComponentStaleError())]
					})
					.then((message) => {
						setTimeout(() => {
							message.delete();
						}, 5000);
					});
			});
		}

		const permissionsArray = interaction.values.map((p) => PermissionBitmap[p as Permission]);
		const permissions = permissionsArray.reduce((acc, p) => acc | p, 0);

		await updatePermissions(
			PermissionCommands.editPermissionMentionable.id,
			interaction.guildId!,
			permissions
		).then(async () => {
			await interaction.followUp({
				embeds: [
					createEmbed(PermissionManageSuccessEmbed(PermissionCommands.editPermissionMentionable!))
				]
			});
		});
	}

	static getPermissionChoices(defaultValues: string[] = []) {
		return (Object.keys(PermissionBitmap) as Permission[]).map((p) => ({
			label: getPermissionDescription(p),
			value: p,
			default: defaultValues.includes(p)
		}));
	}
}
