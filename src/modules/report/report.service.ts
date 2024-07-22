import { Paragraph, ReportAction } from '@prisma/client';
import { CommandInteraction, GuildMember, ModalSubmitInteraction } from 'discord.js';
import { FormatError } from '../../embed/data/genericEmbeds.js';
import { ParagraphNotFoundError } from '../../embed/data/paragraphEmbeds.js';
import { ReportCreated, ReportNotFoundError } from '../../embed/data/reportEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
import { createReportModal } from './report.components.js';
import { createDBReport, getReport, updateReport, warnMember } from './report.helper.js';

export async function createReport(
	interaction: CommandInteraction,
	paragraph: Paragraph | null,
	type: string,
	duration: number | null,
	member: GuildMember,
	delDays: number
) {
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

	const report = await createDBReport({
		type: type === ReportAction.BAN && duration ? ReportAction.TEMP_BAN : (type as ReportAction),
		user: member,
		issuer: interaction.member as GuildMember,
		paragraph,
		guildId: interaction.guildId!,
		duration,
		delDays
	});

	await interaction.showModal(createReportModal(member, report));
}

export async function reportModal(interaction: ModalSubmitInteraction) {
	const [reason, id] = ['reason', 'id'].map((key) => interaction.fields.getTextInputValue(key));

	let report = await getReport(+id, interaction.guildId!);

	if (!report) {
		return interaction.followUp({
			ephemeral: true,
			embeds: [createEmbed(ReportNotFoundError())]
		});
	}

	report = await updateReport(report.number, report.guildId, { reason });

	switch (report.action) {
		case ReportAction.WARN:
			warnMember(report);
			break;
	}

	await interaction.followUp({
		embeds: [createEmbed(ReportCreated(report))]
	});
}
