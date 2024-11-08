import { TimeFormat } from '@discordx/utilities';
import { ModerationAction, Paragraph, Moderation as PrismaModeration } from '@prisma/client';
import * as chrono from 'chrono-node';
import { AutocompleteInteraction } from 'discord.js';
import { NPLAYModerationBot } from '../../bot.js';
import {
	Moderation,
	ModerationActionType,
	ModerationOptions,
	ModerationStatus
} from './moderate.types.js';

/**
 * Get a moderation by its id.
 * @param id The id of the moderation.
 * @returns The moderation or null if it does not exist.
 */
export function getReport(id: string): Promise<Moderation | null> {
	return NPLAYModerationBot.db.moderation.findFirst({
		where: {
			OR: [
				{
					id
				},
				{
					number: isNaN(+id) ? -1 : +id
				}
			]
		},
		include: {
			paragraph: true
		}
	});
}

/**
 * Create a new moderation.
 * @param data The data required to create the moderation.
 * @param reason The reason for the moderation. Optional.
 * @returns The created moderation.
 */
export async function createDBReport(data: ModerationOptions, reason?: string) {
	const nextNumber = await NPLAYModerationBot.db.moderation
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

	return NPLAYModerationBot.db.moderation.create({
		data: {
			number: nextNumber,
			action:
				data.type == ModerationAction.BAN && data.duration
					? ModerationAction.TEMP_BAN
					: (data.type as ModerationAction),
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
 * Update a moderation.
 * @param id The id of the moderation.
 * @param data The data to update.
 * @returns The updated moderation.
 */
export function updateModeration(id: string, data: Partial<PrismaModeration>): Promise<Moderation> {
	return NPLAYModerationBot.db.moderation.update({
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
	return Object.entries(ModerationActionType)
		.map(([key, value]) => ({
			name: value,
			value: key
		}))
		.filter((choice) => choice.value !== ModerationAction.TEMP_BAN);
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

	const date = chrono.de.parseDate(
		value,
		{
			instant: new Date(),
			timezone: 'Europe/Berlin'
		},
		{ forwardDate: true }
	);

	if (isNaN(date.getTime())) {
		return 'Das Dauer konnte nicht gelesen werden, bitte versuche es erneut.';
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

/**
 * Create a moderation field for a modlog embed.
 * @param moderation The moderation to create the field for.
 * @returns The created field.
 */
export function createModlogModerationField(
	moderation: PrismaModeration & { paragraph?: Paragraph | null }
) {
	return {
		name: `#${moderation.number} - ${moderation.action} - ${moderation.id}`,
		value:
			`**Grund:** ${moderation.reason || 'Kein Grund angegeben'}\n` +
			`**Dauer:** ${moderation.duration ? TimeFormat.Default(moderation.duration) : 'Permanent'}\n` +
			`**Paragraph:** ${moderation.paragraph?.name || 'Kein Paragraph'}\n` +
			`**Status:** ${ModerationStatus[moderation.status]}\n` +
			`**Datum:** ${TimeFormat.Default(moderation.createdAt)}\n` +
			`**Moderator:** <@${moderation.issuerId}>`
	};
}
