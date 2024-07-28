import { Paragraph } from '@prisma/client';
import { EmbedBuilder } from 'discord.js';
import { EmbedColors } from '../embed.js';

export function ParagraphCreatedEmbed(paragraph: Paragraph) {
	return new EmbedBuilder()
		.setTitle(`Regelparagraph ${paragraph.name} erstellt`)
		.setDescription(`Der Regelparagraph \`${paragraph.name}\` wurde erfolgreich erstellt.`)
		.addFields([
			{
				name: "Zusammenfassung",
				value: paragraph.summary
			},
			{
				name: "Inhalt",
				value: paragraph.content
			}
		])
		.setColor(EmbedColors.SUCCESS);
}

export function ParagraphShowEmbed(paragraph: Paragraph) {
	return new EmbedBuilder()
		.setTitle(`Regelparagraph ${paragraph.name}`)
		.setDescription(paragraph.summary)
		.addFields([{
			name: "Inhalt",
			value: paragraph.content
		}])
		.setColor(EmbedColors.DEFAULT);
}

export function ParagraphDeletedEmbed(paragraph: Paragraph) {
	return new EmbedBuilder()
		.setTitle(`Regelparagraph ${paragraph.name} gelöscht`)
		.setDescription(`Der Regelparagraph \`${paragraph.name}\` wurde erfolgreich gelöscht.`)
		.setColor(EmbedColors.SUCCESS);

}

export function ParagraphLimitReachedError() {
	return new EmbedBuilder()
		.setTitle("Maximale Anzahl an Paragraphen erreicht")
		.setDescription("Es können maximal 25 Paragraphen erstellt werden.")
		.setColor(EmbedColors.ERROR);
}

export function ParagraphNotFoundError() {
	return new EmbedBuilder()
		.setTitle("Regelparagraph nicht gefunden")
		.setDescription("Der angegebene Regelparagraph konnte nicht gefunden werden.")
		.setColor(EmbedColors.ERROR);
}
