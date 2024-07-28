import { ReportAction, ReportStatus } from '@prisma/client';
import { GuildMember } from 'discord.js';
import { NPLAYModerationBot } from '../../bot.js';
import { KickEmbed, RevertEmbed, TimeoutEmbed, WarnEmbed } from '../../embed/data/reportEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
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

	get member(): Promise<GuildMember | null> {
		return NPLAYModerationBot.Client.guilds.fetch(this.report.guildId).then(async (guild) => {
			try {
				return await guild.members.fetch(this.report.userId);
			} catch {
				return null;
			}
		});
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
			case ReportAction.KICK:
				await this.kickMember();
				break;
		}

		await updateReport(this.report.id, { status: ReportStatus.EXECUTED });
	}

	/**
	 * Reverts the report, marks it as reverted in the database and sends a message to the reported user.
	 * @param reverterId The id of the user who reverted the report.
	 */
	public async revert(reverterId: string | "system") {
		await updateReport(this.report.id, { status: ReportStatus.REVERTED });

		switch (this.report.action) {
			case ReportAction.TIMEOUT:
				const member = await this.member;
				console.log(member?.displayName);
				if (member) {
					console.log(member.isCommunicationDisabled());
					await member.timeout(null);
				}
				break;

			case ReportAction.TEMP_BAN || ReportAction.BAN:
				const guild = await NPLAYModerationBot.Client.guilds.fetch(this.report.guildId);
				await guild.bans.remove(this.report.userId);
				break;
		}

		const guild = await NPLAYModerationBot.Client.guilds.fetch(this.report.guildId).catch(() => {
			console.error(`Could not fetch guild ${this.report.guildId}`);
		});

		const guildName = guild ? guild.name : 'Unbekannt';

		await NPLAYModerationBot.Client.users.fetch(this.report.userId).then(async (user) => {
			await user
				.send({
					embeds: [createEmbed(RevertEmbed(this.report, guildName, reverterId))]
				})
				.catch(() => {
					console.error(`Could not send revert message to ${user.username}`);
				});
		});
	}

	private async warnMember() {
		const member = await this.member;
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
		const member = await this.member;
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

	private async kickMember() {
		const member = await this.member;
		if (!member) return;

		// IMPORTANT: Send the message before kicking the member!!
		member
			.send({
				embeds: [createEmbed(KickEmbed(this.report, member.guild.name))]
			})
			.catch(() => {
				console.error(`Could not send kick message to ${member.displayName}`);
			})
			.finally(async () => {
				await member.kick(this.report.reason || 'Kein Grund angegeben');
			});
	}

	/**
	 * Creates a new NPLAYReport instance from a Report object.
	 * @param report The report object to create the instance from.
	 */
	public static fromReport(report: Report) {
		const data: ReportOptions = {
			type: report.action,
			reportedUserId: report.userId,
			issuerId: report.issuerId,
			paragraph: report.paragraph,
			guildId: report.guildId,
			duration: report.duration,
			delDays: report.delDays,
			message: report.message
		};

		const nplayReport = new NPLAYReport(data, report.reason || undefined);
		nplayReport._report = report;

		return nplayReport;
	}
}