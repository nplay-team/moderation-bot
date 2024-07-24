import { EmbedColors } from '../embed.js';
import { EmbedBuilder } from 'discord.js';
import { TimeFormat } from '@discordx/utilities';
import { ReportActionType, Report } from '../../modules/report/report.types.js';

export function ReportCreated(report: Report) {
	const embed = new EmbedBuilder()
		.setTitle('Report erstellt')
		.setDescription(`Der Report wurde erfolgreich erstellt.`)
		.addFields(
			{
				name: 'ID',
				value: `#${report.number}`,
				inline: true
			},
			{
				name: 'Grund',
				value: report.reason || 'Kein Grund angegeben'
			},
			{
				name: 'Paragraph',
				value: report.paragraph ? report.paragraph.name : "Kein Paragraph angegeben",
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
		.setFooter({
			text: report.id
		})
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

export function ReportFailedMissingData() {
	return new EmbedBuilder()
		.setTitle('Report fehlgeschlagen')
		.setDescription('Der Report konnte nicht erstellt werden, da die Daten vom Command nicht vollständig übermittelt wurden. Bitte versuche es erneut.')
		.setColor(EmbedColors.ERROR);
}

export function WarnEmbed(report: Report, guildName: string) {
	return new EmbedBuilder()
		.setTitle('Verwarnung')
		.setDescription(`Du hast eine Verwarnung auf dem **${guildName}** Server erhalten.`)
		.addFields([
			{
				name: "Grund",
				value: report.reason || 'Kein Grund angegeben'
			},
			{
				name: "Regel, gegen die du verstoßen hast",
				value: report.paragraph ? `${report.paragraph.name} - ${report.paragraph.summary}\n${report.paragraph.content}` : "Kein Paragraph angegeben"
			},
			{
				name: "Deine Nachricht",
				value: report.message ? `[Link](${report.message})` : "Keine Nachricht angegeben"
			},
			{
				name: "Moderator",
				value: `<@${report.issuerId}>`
			}
		])
		.setFooter({
			text: report.id
		})
		.setColor(EmbedColors.WARNING);
}
