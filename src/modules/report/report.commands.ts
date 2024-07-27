import { Paragraph, ReportAction } from '@prisma/client';
import {
	ApplicationCommandOptionType,
	CommandInteraction,
	GuildMember,
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
import { ReportExecutionError } from '../../embed/data/reportEmbeds.js';
import { OnlyOnGuild, RequirePermission } from '../permission/permission.guards.js';
import { PermissionBitmapFlags } from '../permission/permission.types.js';
import { createReportModal } from './report.components.js';
import {
	DurationTransformer,
	getActionChoices,
	ParagraphAutocomplete,
	ParagraphTransformer
} from './report.helper.js';
import { pushReportDataToCache, reportModal } from './report.service.js';

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

		@SlashOption(
			{
				name: 'paragraph',
				description: 'Der Absatz des Reports',
				required: false,
				type: ApplicationCommandOptionType.String,
				autocomplete: ParagraphAutocomplete
			},
			ParagraphTransformer
		)
		paragraphPromise: Promise<Paragraph | null>,

		@SlashOption(
			{
				name: 'duration',
				description: 'Die Dauer des Reports (nur für Ban und Timeout), Format: dd.MM.yyyy HH:mm',
				required: false,
				type: ApplicationCommandOptionType.String
			},
			DurationTransformer
		)
		duration: Date | string | null,

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
		if (typeof duration === 'string') {
			await interaction.reply({
				embeds: [ReportExecutionError(duration)],
				ephemeral: true
			});
			return;
		}

		pushReportDataToCache(interaction.id, {
			type: type as ReportAction,
			reportedUserId: member.id,
			issuerId: interaction.member!.user.id,
			paragraph: await paragraphPromise,
			guildId: interaction.guildId!,
			duration,
			delDays,
			message: null
		});

		await interaction.showModal(createReportModal(member, interaction.id));
	}

	@ModalComponent({ id: RegExp('report-.*') })
	async reportModal(interaction: ModalSubmitInteraction) {
		await interaction.deferReply();
		await reportModal(interaction);
	}
}
