import { Paragraph } from '@prisma/client';
import {
	ApplicationCommandOptionType,
	CommandInteraction,
	ModalSubmitInteraction,
	PermissionsBitField
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
import { OnlyOnGuild, RequirePermission } from '../permission/permission.guards.js';
import { PermissionBitmapFlags } from '../permission/permission.types.js';
import { ParagraphTransformer, getParagraphOptions } from '../report/report.helper.js';
import { modalParagraphCreate } from './paragraph.components.js';
import { createParagraph, deleteParagraph, showParagraph } from './paragraph.service.js';

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
		await interaction.showModal(modalParagraphCreate());
	}

	@ModalComponent({ id: 'create-paragraph' })
	async submitModalParagraphCreate(interaction: ModalSubmitInteraction) {
		await interaction.deferReply();
		await createParagraph(interaction);
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
		await showParagraph(interaction, paragraph);
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
		await deleteParagraph(interaction, paragraph);
	}
}
