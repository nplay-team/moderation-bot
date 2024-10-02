import { EmbedColors } from '../embed.js';
import { EmbedBuilder, EmbedField, RestOrArray } from 'discord.js';
import { TimeFormat } from '@discordx/utilities';
import { Moderation, ModerationActionType } from '../../modules/moderation/moderate.types.js';
import { ModerationAction } from '@prisma/client';

const actionColorMap: Record<ModerationAction, number> = {
	WARN: EmbedColors.WARNING,
	TIMEOUT: EmbedColors.WARNING,
	KICK: EmbedColors.ERROR,
	TEMP_BAN: EmbedColors.ERROR,
	BAN: EmbedColors.ERROR
}

export function ModerationCreated(report: Moderation) {
	return new EmbedBuilder()
		.setTitle(`${ModerationActionType[report.action]} erstellt`)
		.setDescription(`Die Moderationshandlung wurde erfolgreich vollstreckt.`)
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

export function ModerationNotFoundError() {
	return new EmbedBuilder()
		.setTitle('Moderation nicht gefunden')
		.setDescription('Die angegebene Moderation konnte nicht gefunden werden.')
		.setColor(EmbedColors.ERROR);
}

export function ModerationExecutionError(message: string) {
	return new EmbedBuilder()
		.setTitle('Die Moderationshandlung konnte nicht vollstreckt werden')
		.setDescription(`\`\`\`${message}\`\`\``)
		.setColor(EmbedColors.ERROR);
}

export function ModerationFailedMissingData() {
	return new EmbedBuilder()
		.setTitle('Moderation fehlgeschlagen')
		.setDescription('Die Moderationshandlung konnte nicht vollstreckt werden, da die Daten vom Command nicht vollständig übermittelt wurden. Bitte versuche es erneut.')
		.setColor(EmbedColors.ERROR);
}

export function ModerationReverted(report: Moderation) {
	return new EmbedBuilder()
		.setTitle('Moderation zurückgenommen')
		.setDescription('Die Moderationshandlung wurde erfolgreich zurückgenommen.')
		.addFields([
			{
				name: 'ID',
				value: `#${report.number}`,
				inline: false
			},
			{
				name: 'Grund',
				value: report.reason || 'Kein Grund angegeben',
				inline: false
			},
			{
				name: 'Moderator',
				value: `<@${report.issuerId}>`,
				inline: false
			}
		])
		.setFooter({
			text: report.id
		})
		.setColor(EmbedColors.SUCCESS);
}

export function RevertEmbed(report: Moderation, guildName: string, reverterId: string) {
	return new EmbedBuilder()
		.setTitle('Moderation zurückgenommen')
		.setDescription(`Die Moderationshandlung mit der ID **#${report.number}** auf dem **${guildName}** Server wurde zurückgenommen. Eventuelle Timeouts oder Bans wurden aufgehoben.`)
		.addFields([{
			name: 'Moderator',
			value: `<@${reverterId}>`,
		}])
		.setFooter({
			text: report.id
		})
		.setColor(EmbedColors.SUCCESS);
}

export function WarnEmbed(report: Moderation, guildName: string) {
	return new EmbedBuilder()
		.setTitle('Verwarnung')
		.setDescription(`Du hast eine Verwarnung auf dem **${guildName}** Server erhalten.`)
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

export function TimeoutEmbed(report: Moderation, guildName: string) {
	return new EmbedBuilder()
		.setTitle('Timeout')
		.setDescription(`Du wurdest für eine bestimmte Zeit auf dem **${guildName}** Server gesperrt.`)
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

export function KickEmbed(report: Moderation, guildName: string) {
	return new EmbedBuilder()
		.setTitle('Kick')
		.setDescription(`Du wurdest vom **${guildName}** Server gekickt.`)
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

export function TempBanEmbed(report: Moderation, guildName: string) {
	return new EmbedBuilder()
		.setTitle('Temporärer Ban')
		.setDescription(`Du wurdest temporär vom **${guildName}** Server gebannt.`)
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

export function BanEmbed(report: Moderation, guildName: string) {
	return new EmbedBuilder()
		.setTitle('Ban')
		.setDescription(`Du wurdest vom **${guildName}** Server permanent gebannt.`) // TODO: Ban appeal
		.addFields(getReportFields(report))
		.setFooter({
			text: report.id
		})
		.setColor(actionColorMap[report.action]);
}

function getReportFields(report: Moderation) {
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
