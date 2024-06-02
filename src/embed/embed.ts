import { EmbedBuilder } from 'discord.js';
import { NPLAYModerationBot } from '../bot.js';

export const EmbedColors = {
	DEFAULT: 0x020c24,
	ERROR: 0xff0000,
	SUCCESS: 0x00ff00,
	WARNING: 0xffff00
};

export function createEmbed(embed: EmbedBuilder) {
	return embed
		.setAuthor({
			name: NPLAYModerationBot.Client.user?.username || 'NPLAY Moderation Bot',
			iconURL: NPLAYModerationBot.Client.user?.displayAvatarURL() || undefined
		})
		.setTimestamp(new Date());
}
