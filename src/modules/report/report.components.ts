import { ActionRowBuilder, GuildMember, ModalBuilder, TextInputBuilder } from 'discord.js';
import { Report } from './report.types.js';

export function createReportModal(member: GuildMember, report: Report) {
	const menu = new ModalBuilder()
		.setTitle(`Report gegen ${member.displayName}`)
		.setCustomId('report');

	const reasonInputComponent = new TextInputBuilder()
		.setLabel('Begründung')
		.setRequired(true)
		.setCustomId('reason')
		.setStyle(2);

	const idInputComponent = new TextInputBuilder()
		.setLabel('ID (NICHT BEARBEITEN)')
		.setValue(report.number.toString())
		.setRequired(true)
		.setCustomId('id')
		.setStyle(1);

	const row1 = new ActionRowBuilder<TextInputBuilder>().addComponents(reasonInputComponent);
	const row2 = new ActionRowBuilder<TextInputBuilder>().addComponents(idInputComponent);

	menu.addComponents(row1, row2);

	return menu;
}
