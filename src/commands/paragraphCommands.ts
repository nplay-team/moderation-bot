import { Paragraph } from '@prisma/client';
import {
	ActionRowBuilder,
	ApplicationCommandOptionType,
	CommandInteraction,
	ModalBuilder,
	ModalSubmitInteraction,
	PermissionsBitField,
	TextInputBuilder
} from 'discord.js';
import {
	Discord,
	Guard,
	ModalComponent,
	Slash,
	SlashChoice,
	SlashGroup,
	SlashOption
} from 'discordx';
import { NPLAYModerationBot } from '../bot.js';
import {
	ParagraphCreatedEmbed,
	ParagraphDeletedEmbed,
	ParagraphLimitReachedError,
	ParagraphNotFoundError,
	ParagraphShowEmbed
} from '../embed/data/paragraphEmbeds.js';
import { createEmbed } from '../embed/embed.js';
import { OnlyOnGuild, RequirePermission } from '../permission/permissionGuard.js';
import { PermissionBitmapFlags } from '../permission/permissions.js';
import { ParagraphTransformer, getParagraphOptions } from '../reports/reportsHelper.js';

@Discord()
@SlashGroup({
	name: 'paragraph',
	description: 'Alle Befehle zum Erstellen und Verwalten von Regelparagraphen',
	dmPermission: false,
	defaultMemberPermissions: [PermissionsBitField.Flags.BanMembers]
})
@SlashGroup('paragraph')
@Guard(RequirePermission(PermissionBitmapFlags.ParagraphManage), OnlyOnGuild)
export abstract class ParagraphCommands {
	@Slash({ name: 'create', description: 'Erstellt einen neuen Regelparagraphen' })
	async createParagraph(interaction: CommandInteraction) {
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

		await interaction.showModal(modal);
	}

	@ModalComponent({ id: 'create-paragraph' })
	async createParagraphModal(interaction: ModalSubmitInteraction) {
		await interaction.deferReply();

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

	@Slash({ name: 'show', description: 'Zeigt einen Regelparagraphen an' })
	async showParagraph(
		@SlashChoice(...(await getParagraphOptions()))
		@SlashOption({
			name: 'name',
			description: 'Der Name des Regelparagraphen',
			required: true,
			type: ApplicationCommandOptionType.String,
			transformer: ParagraphTransformer
		})
		paragraphPromise: Promise<Paragraph | undefined>,

		interaction: CommandInteraction
	) {
		const paragraph = await paragraphPromise;

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

	@Slash({ name: 'delete', description: 'Löscht einen Regelparagraphen' })
	async deleteParagraph(
		@SlashChoice(...(await getParagraphOptions()))
		@SlashOption({
			name: 'name',
			description: 'Der Name des Regelparagraphen',
			required: true,
			type: ApplicationCommandOptionType.String,
			transformer: ParagraphTransformer
		})
		paragraphPromise: Promise<Paragraph | undefined>,

		interaction: CommandInteraction
	) {
		const paragraph = await paragraphPromise;

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
}
