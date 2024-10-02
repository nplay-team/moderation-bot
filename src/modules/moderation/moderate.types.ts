import { ModerationAction, Paragraph, Moderation as PrismaModeration } from '@prisma/client';

/**
 * ReportActionType is a mapping of the ReportAction enum to a string representation.
 * This is used to display the action in a human-readable format.
 */
export const ModerationActionType = {
	WARN: 'Verwarnung',
	TIMEOUT: 'Timeout',
	KICK: 'Kick',
	TEMP_BAN: 'Temporärer Ban',
	BAN: 'Ban'
};

/**
 * Fields that are required to create a moderation.
 */
export type ModerationOptions = {
	type: ModerationAction;
	reportedUserId: string;
	issuerId: string;
	guildId: string;
	paragraph: Paragraph | null;
	duration: Date | null;
	delDays: number | null;
	message: string | null;
};

export type Moderation = PrismaModeration & { paragraph: Paragraph | null };
