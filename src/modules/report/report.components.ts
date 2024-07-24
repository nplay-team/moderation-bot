import { ActionRowBuilder, GuildMember, ModalBuilder, TextInputBuilder } from 'discord.js';

export function createReportModal(member: GuildMember, id: string) {
	const menu = new ModalBuilder()
		.setTitle(`Report gegen ${member.displayName}`)
		.setCustomId(`report-${id}`);

	const reasonInputComponent = new TextInputBuilder()
		.setLabel('Begründung')
		.setRequired(true)
		.setCustomId('reason')
		.setMaxLength(1000)
		.setStyle(2);

	menu.addComponents(new ActionRowBuilder<TextInputBuilder>().addComponents(reasonInputComponent));

	return menu;
}
