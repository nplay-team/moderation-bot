import { Paragraph, Report as PrismaReport, ReportAction } from '@prisma/client';
import { GuildMember } from 'discord.js';

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

export type Report = PrismaReport & { paragraph: Paragraph };
