import { Report as PrismaReport, ReportAction } from '@prisma/client';
import { parse } from 'date-fns';
import { AutocompleteInteraction } from 'discord.js';
import { NPLAYModerationBot } from '@/bot.js';
import { Report, ReportOptions } from './report.types.js';

/**
 * Get a report by its id.
 * @param id The id of the report.
 * @returns The report or null if it does not exist.
 */
export function getReport(id: string) {
	return NPLAYModerationBot.db.report.findUnique({
		where: {
			id
		},
		include: {
			paragraph: true
		}
	});
}

/**
 * Create a new report.
 * @param data The data required to create the report.
 * @param reason The reason for the report. Optional.
 * @returns The created report.
 */
export async function createDBReport(data: ReportOptions, reason?: string) {
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
			reason,
			message: data.message,
			paragraph: data.paragraph
				? {
						connect: {
							id: data.paragraph.id
						}
					}
				: undefined,
			user: {
				connectOrCreate: {
					where: {
						id: data.reportedUserId
					},
					create: {
						id: data.reportedUserId
					}
				}
			},
			issuer: {
				connectOrCreate: {
					where: {
						id: data.issuerId
					},
					create: {
						id: data.issuerId
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
 * @param id The id of the report.
 * @param data The data to update.
 * @returns The updated report.
 */
export function updateReport(id: string, data: Partial<PrismaReport>): Promise<Report> {
	return NPLAYModerationBot.db.report.update({
		where: {
			id
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
		},
		{
			name: 'Timeout',
			value: ReportAction.TIMEOUT
		}
	];

	// return Object.entries(ReportActionType).map(([key, value]) => ({
	// 	name: value,
	// 	value: key
	// }));
}

/**
 * Transform a paragraph id to a paragraph object.
 * @param value The paragraph id.
 * @returns The paragraph object or null if it does not exist.
 */
export function ParagraphTransformer(value?: string) {
	if (!value) return null;
	return NPLAYModerationBot.db.paragraph.findUnique({ where: { id: value } }) || null;
}

/**
 * Transform a date string to a Date object.
 * @param value The date string.
 * @returns The Date object or -1 if the date is invalid.
 */
export function DurationTransformer(value: string | undefined): Date | string | null {
	if (!value) return null;
	const date = parse(value, 'dd.MM.yyyy HH:mm', new Date());

	if (isNaN(date.getTime())) {
		return 'Das Datum konnte nicht gelesen werden, bitte überprüfe die Formatierung dd.MM.yyyy HH:mm';
	}

	if (date.getTime() <= Date.now()) {
		return 'Das angegebene Datum liegt in der Vergangenheit und ist daher ungültig.';
	}

	return date;
}

/**
 * Discordx function to autocomplete paragraphs.
 * @param interaction The autocomplete interaction by discord.js.
 */
export function ParagraphAutocomplete(interaction: AutocompleteInteraction) {
	const query = interaction.options.getString('paragraph');
	if (!query) return;
	NPLAYModerationBot.db.paragraph
		.findMany({
			where: {
				OR: [
					{
						name: {
							contains: query
						}
					},
					{
						summary: {
							contains: query
						}
					},
					{
						content: {
							contains: query
						}
					}
				]
			}
		})
		.then((paragraphs) => {
			const response = paragraphs.map((paragraph) => ({
				name: paragraph.name + ' - ' + paragraph.summary,
				value: paragraph.id
			}));

			interaction.respond(response);
		});
}
