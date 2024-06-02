import { EmbedColors } from '../embed.js';
import { EmbedBuilder, Message } from 'discord.js';
import { Paragraph, Report } from '@prisma/client';
import { TimeFormat } from '@discordx/utilities';
import { ReportActionType } from '../../reports/reportsHelper.js';

export function ReportCreated(report: Report & { paragraph: Paragraph }) {
	const embed = new EmbedBuilder()
		.setTitle('Report erstellt')
		.setDescription(`Der Report wurde erfolgreich erstellt.`)
		.addFields(
			{
				name: 'ID',
				value: report.id,
				inline: true
			},
			{
				name: 'Grund',
				value: report.reason || 'Kein Grund angegeben'
			},
			{
				name: 'Paragraph',
				value: report.paragraph.name,
				inline: true
			},
			{
				name: 'Benutzer',
				value: `<@${report.userId}>`,
				inline: true
			},
			{
				name: 'Moderator',
				value: `<@${report.issuerId}>`,
				inline: true
			},
			{
				name: 'Aktion',
				value: ReportActionType[report.action],
				inline: true
			}
		)
		.setColor(EmbedColors.SUCCESS);

	if (report.duration) {
		embed.addFields({
			name: 'Gültig bis',
			value: TimeFormat.Default(report.duration),
			inline: true
		});
	}

	if (report.delDays) {
		embed.addFields({
			name: 'Nachrichten löschen',
			value: `${report.delDays} Tage`,
			inline: true
		});
	}

	return embed;
}

export function ReportNotFoundError() {
	return new EmbedBuilder()
		.setTitle('Report nicht gefunden')
		.setDescription('Der angegebene Report konnte nicht gefunden werden.')
		.setColor(EmbedColors.ERROR);
}

export function WarnEmbed(report: Report & { paragraph: Paragraph }, guildName: string, message?: Message) {
	return new EmbedBuilder()
		.setTitle('Verwarnung')
		.setDescription(`Du hast eine Verwarnung auf dem **${guildName}** Server erhalten.`)
		.addFields([
			{
				name: "Grund",
				value: report.reason || 'Kein Grund angegeben'
			},
			{
				name: "Regel gegen die du verstoßen hast",
				value: `${report.paragraph.name} - ${report.paragraph.summary}\n${report.paragraph.content}`
			},
			{
				name: "Deine Nachricht",
				value: message?.content || "Keine Nachricht vorhanden"
			},
			{
				name: "Moderator",
				value: `<@${report.issuerId}>`
			}
		])
		.setColor(EmbedColors.WARNING);
}
