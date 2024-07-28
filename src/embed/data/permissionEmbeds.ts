import { EmbedBuilder, GuildMember, Role, User } from 'discord.js';
import { EmbedColors } from '../embed.js';
import { getPermissionDescription, Permission } from '@/modules/permission/permission.types.js';
import { decodePermissions } from '@/modules/permission/permission.helper.js';

export function NoPermissionEmbed(permissions: string[])  {
	return new EmbedBuilder()
		.setTitle("Unzureichende Berechtigung")
		.setDescription(`Du benötigst die Berechtigung(en) \`${permissions.join(", ")}\`, um diesen Befehl auszuführen.`)
		.setColor(EmbedColors.ERROR);
}

export function PermissionListEmbed(permissions: string[]) {
	return new EmbedBuilder()
		.setTitle("Verfügbare Berechtigungen")
		.addFields(permissions.map((p) => ({
			name: `\`${p}\``,
			value: getPermissionDescription(p as Permission),
			inline: true
		})))
		.setColor(EmbedColors.DEFAULT);
}

export function PermissionGetEmbed(permissions: number, mentionable: GuildMember | User | Role) {
	const isUser = mentionable instanceof GuildMember || mentionable instanceof User;
	const name = isUser ? mentionable instanceof GuildMember ? mentionable.user.username : mentionable.username : mentionable.name;

	const decodedPermissions = decodePermissions(permissions);

	return new EmbedBuilder()
		.setTitle(`Berechtigungen von ${name}`)
		.setDescription(decodedPermissions.map((p) => getPermissionDescription(p as Permission)).join("\n") || "Keine Berechtigungen")
		.setColor(EmbedColors.DEFAULT);
}

export function PermissionManageEmbed(mentionable: GuildMember | User | Role) {
	const isUser = mentionable instanceof GuildMember || mentionable instanceof User;
	const name = isUser ? mentionable instanceof GuildMember ? mentionable.user.username : mentionable.username : mentionable.name;

	return new EmbedBuilder()
		.setTitle(`Berechtigungen von ${name} setzten`)
		.setDescription(`Bitte wähle die Berechtigungen in dem Select-Menu aus:`)
		.setColor(EmbedColors.DEFAULT);
}

export function PermissionManageSuccessEmbed(mentionable: GuildMember | User | Role) {
	const isUser = mentionable instanceof GuildMember || mentionable instanceof User;
	const name = isUser ? mentionable instanceof GuildMember ? mentionable.user.username : mentionable.username : mentionable.name;

	return new EmbedBuilder()
		.setTitle(`Berechtigungen von ${name} aktualisiert`)
		.setDescription(`Die Berechtigungen von ${name} wurden erfolgreich aktualisiert.`)
		.setColor(EmbedColors.SUCCESS);
}

export function PermissionSetErrorEmbed() {
	return new EmbedBuilder()
		.setTitle("Fehler beim Setzen der Berechtigungen")
		.setDescription("Es ist ein Fehler beim Setzen der Berechtigungen aufgetreten. Der Benutzer oder die Rolle ist nicht in der Lage, Berechtigungen zu erhalten.")
		.setColor(EmbedColors.ERROR);
}
