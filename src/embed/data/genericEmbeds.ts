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
