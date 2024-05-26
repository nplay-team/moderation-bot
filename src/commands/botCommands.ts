import { CommandInteraction, PermissionsBitField } from 'discord.js';
import { Discord, Guard, Slash, SlashGroup } from 'discordx';
import { RequirePermission } from '../permission/permissionGuard.js';
import { PermissionBitmapFlags } from '../permission/permissions.js';
import { NPLAYModerationBot } from '../bot.js';

@Discord()
@SlashGroup({
	name: 'bot',
	description: 'Alle Befehle zum Verwalten des Bots',
	dmPermission: false,
	defaultMemberPermissions: [PermissionsBitField.Flags.Administrator]
})
@SlashGroup('bot')
@Guard(RequirePermission(PermissionBitmapFlags.Administrator))
export abstract class BotCommands {
	@Slash({ name: 'restart', description: 'Startet den Bot neu' })
	async restartBot(interaction: CommandInteraction) {
		await interaction.reply({ content: 'Der Bot wird neu gestartet...', ephemeral: true });
		await NPLAYModerationBot.restart();
	}
}
