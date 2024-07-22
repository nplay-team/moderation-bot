import { Paragraph, Report as PrismaReport } from '@prisma/client';
import { Message } from 'discord.js';
import { NPLAYModerationBot } from '../../bot.js';
import { WarnEmbed } from '../../embed/data/reportEmbeds.js';
import { createEmbed } from '../../embed/embed.js';

type Report = PrismaReport & { paragraph: Paragraph };

/**
 * Warn a member by sending them a message.
 * @param report The report to warn the member about.
 * @param message The warned message, if exists.
 */
export function warnMember(report: Report, message?: Message) {
	const member = NPLAYModerationBot.Client.guilds.cache
		.get(report.guildId)
		?.members.cache.get(report.userId);
	if (!member) return;
	member
		.send({
			embeds: [createEmbed(WarnEmbed(report, member.guild.name, message))]
		})
		.catch(() => {
			console.error(`Could not send warn message to ${member.displayName}`);
		});
}

export function timeoutMember() {}

export function kickMember() {}

export function banMember() {}

export function tempBanMember() {}
