import { CommandInteraction, ModalSubmitInteraction } from 'discord.js';
import {
	ModerationCreated,
	ModerationExecutionError,
	ModerationFailedMissingData,
	ModerationNotFoundError,
	ModerationReverted
} from '../../embed/data/moderationEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
import { NPLAYModeration } from './NPLAYModeration.js';
import { getReport } from './moderate.helper.js';
import { ModerationOptions } from './moderate.types.js';

let reportDataCache: Record<string, ModerationOptions> = {};

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
			embeds: [createEmbed(ModerationFailedMissingData())]
		});
		return;
	}

	const report = new NPLAYModeration(data, reason);
	await report.create();
	await report
		.execute()
		.catch((e) => {
			interaction.followUp({
				embeds: [createEmbed(ModerationExecutionError(e.message))]
			});
		})
		.then(() => {
			interaction.followUp({
				embeds: [createEmbed(ModerationCreated(report.report))]
			});
		});
}

export function pushReportDataToCache(id: string, data: ModerationOptions) {
	reportDataCache[id] = data;
}

export function pullReportDataFromCache(id: string): ModerationOptions | undefined {
	const data = reportDataCache[id];
	delete reportDataCache[id];
	return data;
}

export async function revertReport(id: string, interaction: CommandInteraction) {
	const report = await getReport(id);
	if (!report || (report.status !== 'EXECUTED' && report.status !== 'DONE')) {
		await interaction.followUp({
			embeds: [createEmbed(ModerationNotFoundError())]
		});
		return;
	}

	NPLAYModeration.fromReport(report)
		.revert(interaction.member!.user.id)
		.then(() => {
			interaction.followUp({
				embeds: [createEmbed(ModerationReverted(report))]
			});
		})
		.catch((e) => {
			interaction.followUp({
				embeds: [createEmbed(ModerationExecutionError(e.message))]
			});
		});
}
