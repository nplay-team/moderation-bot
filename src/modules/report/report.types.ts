import { Paragraph, Report as PrismaReport, ReportAction } from '@prisma/client';

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
	reportedUserId: string;
	issuerId: string;
	guildId: string;
	paragraph: Paragraph | null;
	duration: number | null;
	delDays: number | null;
	message: string | null;
};

export type Report = PrismaReport & { paragraph: Paragraph | null };
