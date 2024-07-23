import { ActionRowBuilder, GuildMember, ModalBuilder, TextInputBuilder } from 'discord.js';
import { ReportOptions } from './report.types.js';

export function createReportModal(member: GuildMember, data: ReportOptions) {
	const menu = new ModalBuilder()
		.setTitle(`Report gegen ${member.displayName}`)
		.setCustomId('report');

	const reasonInputComponent = new TextInputBuilder()
		.setLabel('Begründung')
		.setRequired(true)
		.setCustomId('reason')
		.setMaxLength(1000)
		.setStyle(2);

	const dataInputComponent = new TextInputBuilder()
		.setLabel('Daten - !NICHT ÄNDERN!')
		.setRequired(true)
		.setCustomId('data')
		.setStyle(2)
		.setValue(btoa(JSON.stringify(data)));

	const row1 = new ActionRowBuilder<TextInputBuilder>().addComponents(reasonInputComponent);
	const row2 = new ActionRowBuilder<TextInputBuilder>().addComponents(dataInputComponent);

	menu.addComponents(row1, row2);

	return menu;
}
