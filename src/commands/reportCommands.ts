import { Paragraph, ReportAction } from '@prisma/client';
import {
	ActionRowBuilder,
	ApplicationCommandOptionType,
	CommandInteraction,
	GuildMember,
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
import { FormatError } from '../embed/data/genericEmbeds.js';
import { ParagraphNotFoundError } from '../embed/data/paragraphEmbeds.js';
import { ReportCreated, ReportNotFoundError } from '../embed/data/reportEmbeds.js';
import { createEmbed } from '../embed/embed.js';
import { OnlyOnGuild, RequirePermission } from '../permission/permissionGuard.js';
import { PermissionBitmapFlags } from '../permission/permissions.js';
import { warnMember } from '../reports/reportActions.js';
import {
	DurationTransformer,
	ParagraphTransformer,
	createReport,
	getActionChoices,
	getParagraphOptions,
	getReport,
	updateReport
} from '../reports/reportsHelper.js';

@Discord()
@SlashGroup({
	name: 'report',
	description: 'Alle Befehle zum Erstellen und Verwalten von Reports',
	dmPermission: false,
	defaultMemberPermissions: [PermissionsBitField.Flags.BanMembers]
})
@SlashGroup('report')
@Guard(RequirePermission(PermissionBitmapFlags.ReportRead), OnlyOnGuild)
export abstract class ReportCommands {
	@Slash({
		name: 'create',
		description: 'Erstellt einen neuen Report'
	})
	@Guard(RequirePermission(PermissionBitmapFlags.ReportCreate))
	async createReport(
		@SlashOption({
			name: 'member',
			description: 'Der Benutzer, gegen den der Report erstellt werden soll',
			required: true,
			type: ApplicationCommandOptionType.User
		})
		member: GuildMember,

		@SlashChoice(...getActionChoices())
		@SlashOption({
			name: 'type',
			description: 'Der Typ des Reports',
			required: true,
			type: ApplicationCommandOptionType.String
		})
		type: string,

		@SlashChoice(...(await getParagraphOptions()))
		@SlashOption({
			name: 'paragraph',
			description: 'Der Absatz des Reports',
			required: true,
			type: ApplicationCommandOptionType.String,
			transformer: ParagraphTransformer
		})
		paragraphPromise: Promise<Paragraph | null>,

		@SlashOption({
			name: 'duration',
			description: 'Die Dauer des Reports (nur für Ban und Timeout), Format: dd.MM.yyyy HH:mm',
			required: false,
			type: ApplicationCommandOptionType.String,
			transformer: DurationTransformer
		})
		duration: number | null,

		@SlashOption({
			name: 'del-days',
			description:
				'Die Anzahl der Tage, für die die Nachrichten des Benutzers gelöscht werden sollen (nur für Ban)',
			required: false,
			type: ApplicationCommandOptionType.Integer,
			minValue: 1,
			maxValue: 7
		})
		delDays: number,

		interaction: CommandInteraction
	) {
		const paragraph = await paragraphPromise;

		if (!paragraph) {
			return interaction.reply({
				ephemeral: true,
				embeds: [createEmbed(ParagraphNotFoundError())]
			});
		}

		if (duration === -1) {
			return interaction.reply({
				ephemeral: true,
				embeds: [createEmbed(FormatError('duration', 'dd.MM.yyyy HH:mm'))]
			});
		}

		const report = await createReport({
			type: type === ReportAction.BAN && duration ? ReportAction.TEMP_BAN : (type as ReportAction),
			user: member,
			issuer: interaction.member as GuildMember,
			paragraph,
			guildId: interaction.guildId!,
			duration,
			delDays
		});

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
			.setValue(report.id)
			.setRequired(true)
			.setCustomId('id')
			.setStyle(1);

		const row1 = new ActionRowBuilder<TextInputBuilder>().addComponents(reasonInputComponent);
		const row2 = new ActionRowBuilder<TextInputBuilder>().addComponents(idInputComponent);

		menu.addComponents(row1, row2);

		await interaction.showModal(menu);
	}

	@ModalComponent({ id: 'report' })
	async reportModal(interaction: ModalSubmitInteraction) {
		await interaction.deferReply();

		const [reason, id] = ['reason', 'id'].map((key) => interaction.fields.getTextInputValue(key));

		console.log(reason, id);

		let report = await getReport(id);

		if (!report) {
			return interaction.followUp({
				ephemeral: true,
				embeds: [createEmbed(ReportNotFoundError())]
			});
		}

		report = await updateReport(report.id, { reason });

		switch (report.action) {
			case ReportAction.WARN:
				warnMember(report);
				break;
		}

		await interaction.followUp({
			embeds: [createEmbed(ReportCreated(report))]
		});
	}
}
