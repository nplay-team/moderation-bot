import { CommandInteraction, EmbedBuilder, GuildMember, ModalSubmitInteraction } from 'discord.js';
import {
	ModerationCreated,
	ModerationExecutionError,
	ModerationFailedMissingData,
	ModerationNotFoundError,
	ModerationReverted
} from '../../embed/data/moderationEmbeds.js';
import { createEmbed, EmbedColors } from '../../embed/embed.js';
import { NPLAYModeration } from './NPLAYModeration.js';
import { createModlogModerationField, getReport } from './moderate.helper.js';
import { ModerationOptions, ModerationStatus } from './moderate.types.js';
import { TimeFormat } from '@discordx/utilities';
import { NPLAYModerationBot } from '../../bot.js';

let reportDataCache: Record<string, ModerationOptions> = {};

setInterval(
	() => {
		reportDataCache = {};
	},
	1000 * 60 * 5
); // Clear cache every 5 minutes

export async function reportModal(interaction: ModalSubmitInteraction) {
	const reason = interaction.fields.getTextInputValue('reason');

	const data = pullReportDataFromCache(interaction.customId.split('-')[1]);

	if (!data) {
		await interaction.followUp({
			embeds: [createEmbed(ModerationFailedMissingData())]
		});
		return;
	}
	
	// TODO: Check if member is timeoutable/bannable by the bot

	const report = new NPLAYModeration(data, reason);
	await report.create();
	await report
		.execute()
		.catch((e) => {
			return interaction.followUp({
				embeds: [createEmbed(ModerationExecutionError(e.message))]
			});
		})
		.then(() => {
			interaction.followUp({
				embeds: [createEmbed(ModerationCreated(report.report))]
			});
		});
}

export function pushReportDataToCache(id: string, data: ModerationOptions) {
	reportDataCache[id] = data;
}

export function pullReportDataFromCache(id: string): ModerationOptions | undefined {
	const data = reportDataCache[id];
	delete reportDataCache[id];
	return data;
}

export async function revertReport(id: string, interaction: CommandInteraction) {
	const report = await getReport(id);
	
	if (!report || report.status === "REVERTED") {
		await interaction.followUp({
			embeds: [createEmbed(ModerationNotFoundError())]
		});
		return;
	}

	NPLAYModeration.fromReport(report)
		.revert(interaction.member!.user.id)
		.then(() => {
			interaction.followUp({
				embeds: [createEmbed(ModerationReverted(report))]
			});
		})
		.catch((e) => {
			interaction.followUp({
				embeds: [createEmbed(ModerationExecutionError(e.message))]
			});
		});
}

export async function generateModlog(guildMember: GuildMember, interaction: CommandInteraction) {
	const moderations = await NPLAYModerationBot.db.moderation.findMany({
		where: {
			userId: guildMember.id,
			guildId: interaction.guildId!,
			NOT: {
				status: "REVERTED"
			} 
		},
		include: {
			paragraph: true
		}
	})
	
	const infoEmbed = new EmbedBuilder()
		.setTitle("NPLAY-Moderation Datenauskunft")
		.addFields([
			{
				name: "Name",
				value: guildMember.user.username,
			},
			{
				name: "Nutzer-ID",
				value: guildMember.user.id,
			},
			{
				name: "Avatar",
				value: `[Link](${guildMember.user.displayAvatarURL()})`,
			},
			{
				name: "Rollen",
				value: guildMember.roles.cache.filter((role) => !role.name.includes("everyone")).map((role) => `<@&${role.id}>`).join(" "),
			},
			{
				name: "Erstellt am",
				value: `${TimeFormat.Default(guildMember.user.createdAt)} (${TimeFormat.RelativeTime(guildMember.user.createdAt)})`,
			},
			{
				name: "Beigetreten am",
				value: `${TimeFormat.Default(guildMember.joinedAt!)} (${TimeFormat.RelativeTime(guildMember.joinedAt!)})`,
			}
		])
		.setThumbnail(guildMember.user.displayAvatarURL())
		.setColor(EmbedColors.DEFAULT);
	
	const moderationsEmbed = new EmbedBuilder()
		.setTitle("Moderationsverlauf")
		.addFields(moderations.map((moderation) => createModlogModerationField(moderation)))
		.setThumbnail(NPLAYModerationBot.Client.user!.displayAvatarURL())
		.setColor(EmbedColors.DEFAULT);
	
	await interaction.followUp({
		embeds: [createEmbed(infoEmbed), moderationsEmbed]
	});
}
