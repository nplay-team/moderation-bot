import { EmbedColors } from '../embed.js';
import { EmbedBuilder, EmbedField, RestOrArray } from 'discord.js';
import { TimeFormat } from '@discordx/utilities';
import { Report } from '../../modules/report/report.types.js';
import { ReportAction } from '@prisma/client';

const actionColorMap: Record<ReportAction, number> = {
	WARN: EmbedColors.WARNING,
	TIMEOUT: EmbedColors.WARNING,
	KICK: EmbedColors.ERROR,
	TEMP_BAN: EmbedColors.ERROR,
	BAN: EmbedColors.ERROR
}

export function ReportCreated(report: Report) {
	return new EmbedBuilder()
		.setTitle('Report erstellt')
		.setDescription(`Der Report wurde erfolgreich erstellt.`)
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

export function ReportNotFoundError() {
	return new EmbedBuilder()
		.setTitle('Report nicht gefunden')
		.setDescription('Der angegebene Report konnte nicht gefunden werden.')
		.setColor(EmbedColors.ERROR);
}

export function ReportExecutionError(message: string) {
	return new EmbedBuilder()
		.setTitle('Report konnte nicht erstellt werden')
		.setDescription(`\`\`\`${message}\`\`\``)
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
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

export function TimeoutEmbed(report: Report, guildName: string) {
	return new EmbedBuilder()
		.setTitle('Timeout')
		.setDescription(`Du wurdest für eine bestimmte Zeit auf dem **${guildName}** Server gesperrt.`)
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

export function KickEmbed(report: Report, guildName: string) {
	return new EmbedBuilder()
		.setTitle('Kick')
		.setDescription(`Du wurdest vom **${guildName}** Server gekickt.`)
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

function getReportFields(report: Report) {
	const base: RestOrArray<EmbedField> = [
		{
			name: 'ID',
			value: `#${report.number}`,
			inline: false
		},
		{
			name: "Grund",
			value: report.reason || 'Kein Grund angegeben',
			inline: false
		},
		{
			name: "Regel, gegen die du verstoßen hast",
			value: report.paragraph ? `${report.paragraph.name} - ${report.paragraph.summary}\n${report.paragraph.content}` : "Kein Paragraph angegeben",
			inline: false
		},
		{
			name: "Deine Nachricht",
			value: report.message ? `[Link](${report.message})` : "Keine Nachricht angegeben",
			inline: false
		},
		{
			name: "Moderator",
			value: `<@${report.issuerId}>`,
			inline: false
		}
	]
	
	if (report.duration) {
		base.push({
			name: 'Gültig bis',
			value: TimeFormat.Default(report.duration),
			inline: false
		});
	}
	
	return base;
}
