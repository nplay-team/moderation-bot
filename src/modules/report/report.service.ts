import { ReportAction, ReportStatus } from '@prisma/client';
import { GuildMember, ModalSubmitInteraction } from 'discord.js';
import { NPLAYModerationBot } from '../../bot.js';
import { ReportCreated, WarnEmbed } from '../../embed/data/reportEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
import { createDBReport, updateReport } from './report.helper.js';
import { Report, ReportOptions } from './report.types.js';

export async function reportModal(interaction: ModalSubmitInteraction) {
	const [reason, dataBase64] = ['reason', 'data'].map((key) =>
		interaction.fields.getTextInputValue(key)
	);

	const data: ReportOptions = JSON.parse(atob(dataBase64));

	const report = new NPLAYReport(data, reason);
	await report.create();
	await report.execute();

	await interaction.followUp({
		embeds: [createEmbed(ReportCreated(report.report))]
	});
}

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

	public async create() {
		this._report = await createDBReport(this._data, this._reason);
	}

	public async execute() {
		switch (this.report.action) {
			case ReportAction.WARN:
				this.warnMember();
				break;
		}

		await updateReport(this.report.id, { status: ReportStatus.EXECUTED });
	}

	private warnMember() {
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
}
