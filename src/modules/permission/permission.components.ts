import {
	ActionRowBuilder,
	MessageActionRowComponentBuilder,
	StringSelectMenuBuilder
} from 'discord.js';
import { decodePermissions, getPermissionChoices } from './permission.helper.js';

export function permissionMenu(permissions: number | null) {
	const permissionsArray = decodePermissions(permissions || 0);

	const options = getPermissionChoices(permissionsArray);

	const menu = new StringSelectMenuBuilder()
		.setOptions(options)
		.setCustomId('edit-permissions')
		.setMinValues(0)
		.setMaxValues(options.length <= 25 ? options.length : 25)
		.setPlaceholder('Keine Berechtigungen');

	return new ActionRowBuilder<MessageActionRowComponentBuilder>().addComponents(menu);
}
