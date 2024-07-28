import { Paragraph } from '@prisma/client';
import { CommandInteraction, ModalSubmitInteraction } from 'discord.js';
import { NPLAYModerationBot } from '../../bot.js';
import {
	ParagraphCreatedEmbed,
	ParagraphDeletedEmbed,
	ParagraphLimitReachedError,
	ParagraphNotFoundError,
	ParagraphShowEmbed
} from '../../embed/data/paragraphEmbeds.js';
import { createEmbed } from '../../embed/embed.js';

export async function createParagraph(interaction: ModalSubmitInteraction) {
	const paragraphCount = await NPLAYModerationBot.db.paragraph.count({
		where: {
			guildId: interaction.guildId!
		}
	});

	if (paragraphCount >= 25) {
		return await interaction.followUp({
			ephemeral: true,
			embeds: [createEmbed(ParagraphLimitReachedError())]
		});
	}

	const [name, summary, content] = ['name', 'summary', 'content'].map((id) =>
		interaction.fields.getTextInputValue(id)
	);

	NPLAYModerationBot.db.paragraph
		.create({
			data: {
				name,
				summary,
				content,
				guildId: interaction.guildId!
			}
		})
		.then(async (paragraph) => {
			await interaction.followUp({
				embeds: [createEmbed(ParagraphCreatedEmbed(paragraph))]
			});
		});
}

export async function showParagraph(
	interaction: CommandInteraction,
	paragraph: Paragraph | undefined
) {
	if (!paragraph) {
		return await interaction.reply({
			ephemeral: true,
			embeds: [createEmbed(ParagraphNotFoundError())]
		});
	}

	await interaction.reply({
		ephemeral: true,
		embeds: [createEmbed(ParagraphShowEmbed(paragraph))]
	});
}

export async function deleteParagraph(
	interaction: CommandInteraction,
	paragraph: Paragraph | undefined
) {
	if (!paragraph) {
		return await interaction.reply({
			ephemeral: true,
			embeds: [createEmbed(ParagraphNotFoundError())]
		});
	}

	await NPLAYModerationBot.db.paragraph.delete({
		where: {
			id: paragraph.id
		}
	});

	await interaction.reply({
		ephemeral: true,
		embeds: [createEmbed(ParagraphDeletedEmbed(paragraph))]
	});
}
