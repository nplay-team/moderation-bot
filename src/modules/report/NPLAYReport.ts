import { ReportAction, ReportStatus } from '@prisma/client';
import { GuildMember } from 'discord.js';
import { NPLAYModerationBot } from '@/bot.js';
import { TimeoutEmbed, WarnEmbed } from '@/embed/data/reportEmbeds.js';
import { createEmbed } from '@/embed/embed.js';
import { createDBReport, updateReport } from './report.helper.js';
import { Report, ReportOptions } from './report.types.js';

export class NPLAYReport {
	public _report: Report | undefined;

	constructor(
		private _data: ReportOptions,
		private _reason?: string
	) {}

	get report() {
		if (!this._report) throw new Error('Report not initialized yet');
		return this._report;
	}

	get member(): GuildMember | null {
		const member = NPLAYModerationBot.Client.guilds.cache
			.get(this.report.guildId)
			?.members.cache.get(this.report.userId);
		if (!member) return null;
		return member;
	}

	/**
	 * Creates a new report in the database.
	 * Needs to be called before `execute` as initialization.
	 */
	public async create() {
		this._report = await createDBReport(this._data, this._reason);
	}

	/**
	 * Executes the report action.
	 */
	public async execute() {
		switch (this.report.action) {
			case ReportAction.WARN:
				await this.warnMember();
				break;
			case ReportAction.TIMEOUT:
				await this.timeoutMember();
				break;
		}

		await updateReport(this.report.id, { status: ReportStatus.EXECUTED });
	}

	private async warnMember() {
		const member = this.member;
		if (!member) return;

		member
			.send({
				embeds: [createEmbed(WarnEmbed(this.report, member.guild.name))]
			})
			.catch(() => {
				console.error(`Could not send warn message to ${member.displayName}`);
			});
	}

	private async timeoutMember() {
		const member = this.member;
		if (!member) return;

		if (!this.report.duration) {
			throw new Error('Der Parameter "duration" ist nicht gesetzt, wird aber benötigt.');
		}

		if (this.report.duration.getTime() - this.report.createdAt.getTime() > 2419200000) {
			throw new Error('Die maximale Dauer für einen Timeout beträgt 28 Tage.');
		}

		await member.timeout(
			this.report.duration.getTime() - this.report.createdAt.getTime(),
			this.report.reason || 'Kein Grund angegeben'
		);

		member
			.send({
				embeds: [createEmbed(TimeoutEmbed(this.report, member.guild.name))]
			})
			.catch(() => {
				console.error(`Could not send timeout message to ${member.displayName}`);
			});
	}
}
