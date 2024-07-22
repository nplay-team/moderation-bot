import { ActionRowBuilder, ModalBuilder, TextInputBuilder } from 'discord.js';

export function modalParagraphCreate() {
	const modal = new ModalBuilder()
		.setTitle(`Neuen Regelparagraph erstellen`)
		.setCustomId('create-paragraph');

	const nameInputComponent = new TextInputBuilder()
		.setCustomId('name')
		.setLabel('Name')
		.setPlaceholder('z.B. 1.1 oder 3.2')
		.setRequired(true)
		.setMaxLength(10)
		.setStyle(1);

	const summaryInputComponent = new TextInputBuilder()
		.setCustomId('summary')
		.setLabel('Kurzbeschreibung')
		.setPlaceholder('z.B. Beleidigungen')
		.setRequired(true)
		.setMaxLength(100)
		.setStyle(1);

	const contentInputComponent = new TextInputBuilder()
		.setCustomId('content')
		.setLabel('Inhalt')
		.setPlaceholder('Der Paragraph')
		.setRequired(true)
		.setMinLength(1)
		.setMaxLength(2000)
		.setStyle(2);

	const row1 = new ActionRowBuilder<TextInputBuilder>().addComponents(nameInputComponent);

	const row2 = new ActionRowBuilder<TextInputBuilder>().addComponents(summaryInputComponent);

	const row3 = new ActionRowBuilder<TextInputBuilder>().addComponents(contentInputComponent);

	modal.addComponents(row1, row2, row3);

	return modal;
}
