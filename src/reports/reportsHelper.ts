import { Paragraph, Report, ReportAction } from '@prisma/client';
import { parse } from 'date-fns';
import { GuildMember } from 'discord.js';
import { NPLAYModerationBot } from '../bot.js';

/**
 * ReportActionType is a mapping of the ReportAction enum to a string representation.
 */
export const ReportActionType = {
	WARN: 'Verwarnung',
	TIMEOUT: 'Timeout',
	KICK: 'Kick',
	TEMP_BAN: 'Temporärer Ban',
	BAN: 'Ban'
};

/**
 * Fields that are required to create a report.
 */
export type ReportOptions = {
	type: ReportAction;
	user: GuildMember;
	issuer: GuildMember;
	paragraph: Paragraph;
	guildId: string;
	duration: number | null;
	delDays?: number;
};

/**
 * Get a report by its id.
 * @param id The id of the report.
 * @returns The report or null if it does not exist.
 */
export function getReport(id: string) {
	return NPLAYModerationBot.db.report.findUnique({
		where: {
			id: id
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
export function createReport(data: ReportOptions) {
	return NPLAYModerationBot.db.report.create({
		data: {
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
		}
	});
}

/**
 * Update a report.
 * @param id The id of the report.
 * @param data The data to update.
 * @returns The updated report.
 */
export function updateReport(id: string, data: Partial<Report>) {
	return NPLAYModerationBot.db.report.update({
		where: {
			id: id
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
