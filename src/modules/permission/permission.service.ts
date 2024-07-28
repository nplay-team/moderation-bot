import { GuildMember, Role, StringSelectMenuInteraction, User } from 'discord.js';
import { ComponentStaleError } from '../../embed/data/genericEmbeds.js';
import { PermissionManageSuccessEmbed } from '../../embed/data/permissionEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
import { updatePermissions } from './permission.helper.js';
import { Permission, PermissionBitmap } from './permission.types.js';

export async function editPermissions(
	interaction: StringSelectMenuInteraction,
	permissionId: string | null,
	mentionable: GuildMember | User | Role | null
) {
	if (interaction.message.interaction?.id !== permissionId || !mentionable) {
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

	await updatePermissions(mentionable.id, interaction.guildId!, permissions).then(async () => {
		await interaction.followUp({
			embeds: [createEmbed(PermissionManageSuccessEmbed(mentionable!))]
		});
	});
}
