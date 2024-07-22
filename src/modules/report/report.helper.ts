import { Report as PrismaReport, ReportAction } from '@prisma/client';
import { parse } from 'date-fns';
import { Message } from 'discord.js';
import { NPLAYModerationBot } from '../../bot.js';
import { WarnEmbed } from '../../embed/data/reportEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
import { Report, ReportOptions } from './report.types.js';

/**
 * Get a report by its id.
 * @param number The id of the report.
 * @param guildId The id of the guild.
 * @returns The report or null if it does not exist.
 */
export function getReport(number: number, guildId: string) {
	return NPLAYModerationBot.db.report.findUnique({
		where: {
			id: {
				number: number,
				guildId: guildId
			}
		},
		include: {
			paragraph: true
		}
	});
}

/**
 * Create a new report.
 * @param data The data required to create the report.
 * @returns The created report.
 */
export async function createDBReport(data: ReportOptions) {
	const nextNumber = await NPLAYModerationBot.db.report
		.findFirst({
			where: {
				guildId: data.guildId
			},
			select: {
				number: true
			},
			orderBy: {
				number: 'desc'
			}
		})
		.then((report) => {
			if (!report) return 1;
			return report.number + 1;
		});

	return NPLAYModerationBot.db.report.create({
		data: {
			number: nextNumber,
			action:
				data.type == ReportAction.BAN && data.duration
					? ReportAction.TEMP_BAN
					: (data.type as ReportAction),
			duration: data.duration,
			delDays: data.delDays,
			guildId: data.guildId,
			paragraph: {
				connect: {
					id: data.paragraph.id
				}
			},
			user: {
				connectOrCreate: {
					where: {
						id: data.user.id
					},
					create: {
						id: data.user.id
					}
				}
			},
			issuer: {
				connectOrCreate: {
					where: {
						id: data.issuer.id
					},
					create: {
						id: data.issuer.id
					}
				}
			}
		},
		include: {
			paragraph: true
		}
	});
}

/**
 * Update a report.
 * @param number The id of the report.
 * @param guildId The id of the guild.
 * @param data The data to update.
 * @returns The updated report.
 */
export function updateReport(number: number, guildId: string, data: Partial<PrismaReport>) {
	return NPLAYModerationBot.db.report.update({
		where: {
			id: {
				number: number,
				guildId: guildId
			}
		},
		data: data,
		include: {
			paragraph: true
		}
	});
}

/**
 * Get the choices for the action select menu.
 * @returns The choices for the action select menu.
 */
export function getActionChoices() {
	// TODO: Temporary due to missing support
	return [
		{
			name: 'Verwarnung',
			value: ReportAction.WARN
		}
	];

	// return Object.entries(ReportActionType).map(([key, value]) => ({
	// 	name: value,
	// 	value: key
	// }));
}

/**
 * Get the choices for the paragraph select menu.
 * @returns The choices for the paragraph select menu.
 */
export async function getParagraphOptions() {
	return NPLAYModerationBot.db.paragraph.findMany().then((paragraphs) =>
		paragraphs.map((paragraph) => ({
			name: paragraph.name + ' - ' + paragraph.summary,
			value: paragraph.id
		}))
	);
}

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

/**
 * Transform a paragraph id to a paragraph object.
 * @param value The paragraph id.
 * @returns The paragraph object or null if it does not exist.
 */
export function ParagraphTransformer(value: string) {
	return NPLAYModerationBot.db.paragraph.findUnique({ where: { id: value } });
}

/**
 * Transform a date string to a Date object.
 * @param value The date string.
 * @returns The Date object or -1 if the date is invalid.
 */
export function DurationTransformer(value: string | undefined) {
	if (!value) return null;
	const date = parse(value, 'dd.MM.yyyy HH:mm', new Date());
	if (isNaN(date.getTime())) {
		return -1;
	}
	return date.getTime();
}
