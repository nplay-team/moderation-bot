import { EmbedColors } from '../embed.js';
import { EmbedBuilder } from 'discord.js';

export function CommandOnlyOnGuildError() {
		return new EmbedBuilder()
				.setTitle("Nur auf einem Server möglich")
				.setDescription("Dieser Befehl kann nur in einem Server verwendet werden.")
				.setColor(EmbedColors.ERROR);
}

export function ComponentStaleError() {
		return new EmbedBuilder()
				.setTitle("Komponente veraltet")
				.setDescription("Diese Komponente ist veraltet und kann nicht mehr verwendet werden.")
				.setColor(EmbedColors.ERROR);
}

export function FormatError(field: string, expected: string) {
		return new EmbedBuilder()
				.setTitle("Fehlerhafte Eingabe")
				.setDescription(`Das Feld \`${field}\` muss vom Typ oder im Format \`${expected}\` sein.`)
				.setColor(EmbedColors.ERROR);
}

export function UnknownError(e: unknown) {
		return new EmbedBuilder()
				.setTitle("Unbekannter Fehler")
				.setDescription(`\`\`\`${e}\`\`\``)
				.setColor(EmbedColors.ERROR);
}
