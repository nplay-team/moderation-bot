import { ReportAction } from '@prisma/client';
import { CommandInteraction, ModalSubmitInteraction } from 'discord.js';
import {
	ReportCreated,
	ReportExecutionError,
	ReportFailedMissingData,
	ReportNotFoundError,
	ReportReverted
} from '../../embed/data/reportEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
import { NPLAYReport } from './NPLAYReport.js';
import { getReport } from './report.helper.js';
import { ReportOptions } from './report.types.js';

let reportDataCache: Record<string, ReportOptions> = {};

setInterval(
	() => {
		reportDataCache = {};
	},
	1000 * 60 * 5
); // Clear cache every 5 minutes

export async function reportModal(interaction: ModalSubmitInteraction) {
	const reason = interaction.fields.getTextInputValue('reason');

	const data = pullReportDataFromCache(interaction.customId.split('-')[1]);

	if (!data) {
		await interaction.followUp({
			embeds: [createEmbed(ReportFailedMissingData())]
		});
		return;
	}

	if (data.type === ReportAction.BAN && data.duration) {
		data.type = ReportAction.TEMP_BAN;
	}

	const report = new NPLAYReport(data, reason);
	await report.create();
	await report
		.execute()
		.catch((e) => {
			interaction.followUp({
				embeds: [createEmbed(ReportExecutionError(e.message))]
			});
		})
		.then(() => {
			interaction.followUp({
				embeds: [createEmbed(ReportCreated(report.report))]
			});
		});
}

export function pushReportDataToCache(id: string, data: ReportOptions) {
	reportDataCache[id] = data;
}

export function pullReportDataFromCache(id: string): ReportOptions | undefined {
	const data = reportDataCache[id];
	delete reportDataCache[id];
	return data;
}

export async function revertReport(id: string, interaction: CommandInteraction) {
	const report = await getReport(id);
	if (!report || (report.status !== 'EXECUTED' && report.status !== 'DONE')) {
		await interaction.followUp({
			embeds: [createEmbed(ReportNotFoundError())]
		});
		return;
	}

	NPLAYReport.fromReport(report)
		.revert(interaction.member!.user.id)
		.then(() => {
			interaction.followUp({
				embeds: [createEmbed(ReportReverted(report))]
			});
		})
		.catch((e) => {
			interaction.followUp({
				embeds: [createEmbed(ReportExecutionError(e.message))]
			});
		});
}
