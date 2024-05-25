import { EmbedBuilder, GuildMember, Role, User } from 'discord.js';
import { EmbedColors } from '../embed.js';
import { getPermissionDescription, Permission } from '../../permission/permissions.js';
import { decodePermissions } from '../../permission/permissionHelpers.js';

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

	return new EmbedBuilder()
		.setTitle(`Berechtigungen von ${name}`)
		.setDescription(`${isUser ? "Der Benutzer" : "Die Rolle"} hat die folgenden Berechtigungen:`)
		.addFields(decodePermissions(permissions).map((p) => ({
			name: `\`${p}\``,
			value: getPermissionDescription(p as Permission),
			inline: true
		})))
		.addFields(permissions == 0 ? [{
				name: "Keine Berechtigungen",
				value: "Dieser Benutzer oder diese Rolle hat keine Berechtigungen.",
		}] : [])
		.setColor(EmbedColors.DEFAULT);
}

export function PermissionSetEmbed(permissions: number, mentionable: GuildMember | User | Role) {
	const isUser = mentionable instanceof GuildMember || mentionable instanceof User;
	const name = isUser ? mentionable instanceof GuildMember ? mentionable.user.username : mentionable.username : mentionable.name;

	return new EmbedBuilder()
		.setTitle(`Berechtigungen von ${name} gesetzt`)
		.setDescription(`${isUser ? "Der Benutzer" : "Die Rolle"} hat nun die folgenden Berechtigungen:`)
		.addFields(decodePermissions(permissions).map((p) => ({
			name: `\`${p}\``,
			value: getPermissionDescription(p as Permission),
			inline: true
		})))
		.setColor(EmbedColors.SUCCESS);
}

export function PermissionSetErrorEmbed() {
	return new EmbedBuilder()
		.setTitle("Fehler beim Setzen der Berechtigungen")
		.setDescription("Es ist ein Fehler beim Setzen der Berechtigungen aufgetreten. Der Benutzer oder die Rolle ist nicht in der Lage, Berechtigungen zu erhalten.")
		.setColor(EmbedColors.ERROR);
}
