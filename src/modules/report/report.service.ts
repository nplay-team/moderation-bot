import { ModalSubmitInteraction } from 'discord.js';
import {
	ReportCreated,
	ReportExecutionError,
	ReportFailedMissingData
} from '../../embed/data/reportEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
import { NPLAYReport } from './NPLAYReport.js';
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
