import { ModerationAction, Paragraph } from '@prisma/client';
import {
	ApplicationCommandOptionType,
	CommandInteraction,
	GuildMember,
	ModalSubmitInteraction,
	PermissionsBitField
} from 'discord.js';
import { Discord, Guard, ModalComponent, Slash, SlashGroup, SlashOption } from 'discordx';
import { NPLAYModerationBot } from '../../bot.js';
import { ModerationExecutionError } from '../../embed/data/moderationEmbeds.js';
import { OnlyOnGuild, RequirePermission } from '../permission/permission.guards.js';
import { PermissionBitmapFlags } from '../permission/permission.types.js';
import { createReportModal } from './moderate.components.js';
import {
	DurationTransformer,
	ParagraphAutocomplete,
	ParagraphTransformer
} from './moderate.helper.js';
import { generateModlog, pushReportDataToCache, reportModal, revertReport } from './moderate.service.js';

@Discord()
@SlashGroup({
	name: 'moderate',
	description: 'Alle Befehle zum Moderieren von Servermitgliedern',
	dmPermission: false,
	defaultMemberPermissions: [PermissionsBitField.Flags.BanMembers]
})
@SlashGroup('report')
@Guard(RequirePermission(PermissionBitmapFlags.ModerationRead), OnlyOnGuild)
export abstract class ModerateCommands {
	@Slash({
		name: 'warn',
		description: 'Verwarnt einen Benutzer'
	})
	@SlashGroup('moderate')
	@Guard(RequirePermission(PermissionBitmapFlags.ModerationCreate))
	async moderateWarn(
		@SlashOption({
			name: 'member',
			description: 'Der Benutzer, gegen den vorgegangen werden soll',
			required: true,
			type: ApplicationCommandOptionType.User
		})
		member: GuildMember,

		@SlashOption(
			{
				name: 'paragraph',
				description: 'Der Regelparagraph, gegen den verstoßen wurde',
				required: false,
				type: ApplicationCommandOptionType.String,
				autocomplete: ParagraphAutocomplete
			},
			ParagraphTransformer
		)
		paragraphPromise: Promise<Paragraph | null>,

		interaction: CommandInteraction
	) {
		pushReportDataToCache(interaction.id, {
			type: ModerationAction.WARN,
			reportedUserId: member.id,
			issuerId: interaction.member!.user.id,
			paragraph: await paragraphPromise,
			guildId: interaction.guildId!,
			duration: null,
			delDays: null,
			message: null
		});

		await interaction.showModal(createReportModal(member, interaction.id));
	}

	@Slash({
		name: 'timeout',
		description: 'Gibt einem Benutzer einen Timeout'
	})
	@SlashGroup('moderate')
	@Guard(RequirePermission(PermissionBitmapFlags.ModerationCreate))
	async moderateTimeout(
		@SlashOption({
			name: 'member',
			description: 'Der Benutzer, gegen den vorgegangen werden soll',
			required: true,
			type: ApplicationCommandOptionType.User
		})
		member: GuildMember,

		@SlashOption(
			{
				name: 'end-date',
				description: 'Dauer des Timeouts',
				required: true,
				type: ApplicationCommandOptionType.String
			},
			DurationTransformer
		)
		duration: Date | string | null,

		@SlashOption(
			{
				name: 'paragraph',
				description: 'Der Regelparagraph, gegen den verstoßen wurde',
				required: false,
				type: ApplicationCommandOptionType.String,
				autocomplete: ParagraphAutocomplete
			},
			ParagraphTransformer
		)
		paragraphPromise: Promise<Paragraph | null>,

		interaction: CommandInteraction
	) {
		if (typeof duration === 'string') {
			await interaction.reply({
				embeds: [ModerationExecutionError(duration)],
				ephemeral: true
			});
			return;
		}

		pushReportDataToCache(interaction.id, {
			type: ModerationAction.TIMEOUT,
			reportedUserId: member.id,
			issuerId: interaction.member!.user.id,
			paragraph: await paragraphPromise,
			guildId: interaction.guildId!,
			duration: duration,
			delDays: null,
			message: null
		});

		await interaction.showModal(createReportModal(member, interaction.id));
	}

	@Slash({
		name: 'kick',
		description: 'Kickt einen Benutzer'
	})
	@SlashGroup('moderate')
	@Guard(RequirePermission(PermissionBitmapFlags.ModerationCreate))
	async moderateKick(
		@SlashOption({
			name: 'member',
			description: 'Der Benutzer, gegen den vorgegangen werden soll',
			required: true,
			type: ApplicationCommandOptionType.User
		})
		member: GuildMember,

		@SlashOption(
			{
				name: 'paragraph',
				description: 'Der Regelparagraph, gegen den verstoßen wurde',
				required: false,
				type: ApplicationCommandOptionType.String,
				autocomplete: ParagraphAutocomplete
			},
			ParagraphTransformer
		)
		paragraphPromise: Promise<Paragraph | null>,

		interaction: CommandInteraction
	) {
		pushReportDataToCache(interaction.id, {
			type: ModerationAction.KICK,
			reportedUserId: member.id,
			issuerId: interaction.member!.user.id,
			paragraph: await paragraphPromise,
			guildId: interaction.guildId!,
			duration: null,
			delDays: null,
			message: null
		});

		await interaction.showModal(createReportModal(member, interaction.id));
	}

	@Slash({
		name: 'ban',
		description: 'Bannt einen Benutzer temporär oder permanent'
	})
	@SlashGroup('moderate')
	@Guard(RequirePermission(PermissionBitmapFlags.ModerationCreate))
	async moderateBan(
		@SlashOption({
			name: 'member',
			description: 'Der Benutzer, gegen den vorgegangen werden soll',
			required: true,
			type: ApplicationCommandOptionType.User
		})
		member: GuildMember,

		@SlashOption(
			{
				name: 'paragraph',
				description: 'Der Regelparagraph, gegen den verstoßen wurde',
				required: false,
				type: ApplicationCommandOptionType.String,
				autocomplete: ParagraphAutocomplete
			},
			ParagraphTransformer
		)
		paragraphPromise: Promise<Paragraph | null>,

		@SlashOption(
			{
				name: 'end-date',
				description: 'Dauer des Bans - optional',
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
				embeds: [ModerationExecutionError(duration)],
				ephemeral: true
			});
			return;
		}

		pushReportDataToCache(interaction.id, {
			type: duration ? ModerationAction.TEMP_BAN : ModerationAction.BAN,
			reportedUserId: member.id,
			issuerId: interaction.member!.user.id,
			paragraph: await paragraphPromise,
			guildId: interaction.guildId!,
			duration: duration,
			delDays: delDays,
			message: null
		});

		await interaction.showModal(createReportModal(member, interaction.id));
	}

	@ModalComponent({ id: RegExp('moderate-.*') })
	async reportModal(interaction: ModalSubmitInteraction) {
		await interaction.deferReply();
		await reportModal(interaction);
	}

	@Slash({
		name: 'revert',
		description: 'Revidiert eine Moderationshandlung'
	})
	@SlashGroup('moderate')
	@Guard(RequirePermission(PermissionBitmapFlags.ModerationDelete))
	async revertReport(
		@SlashOption({
			name: 'id',
			description: 'Die ID der Moderationshandlung',
			required: true,
			type: ApplicationCommandOptionType.String
		})
		reportId: string,
		interaction: CommandInteraction
	) {
		await interaction.deferReply();
		await revertReport(reportId, interaction);
	}

	@Slash({
		name: 'info',
		description: 'Gibt alle Moderationshandlungen zu einem Benutzer aus'
	})
	@SlashGroup('moderate')
	@Guard(RequirePermission(PermissionBitmapFlags.ModerationDelete))
	async moderateInfo(
		@SlashOption({
			name: 'member',
			description: 'Der Benutzer, dessen Moderationshandlungen angezeigt werden sollen',
			required: true,
			type: ApplicationCommandOptionType.User
		})
		user: GuildMember,
		interaction: CommandInteraction
	) {
		await interaction.deferReply();
		await generateModlog(user, interaction);
	}
}
