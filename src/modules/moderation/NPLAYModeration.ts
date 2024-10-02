import { ModerationAction, ReportStatus } from '@prisma/client';
import { GuildMember } from 'discord.js';
import { NPLAYModerationBot } from '../../bot.js';
import {
	BanEmbed,
	KickEmbed,
	RevertEmbed,
	TempBanEmbed,
	TimeoutEmbed,
	WarnEmbed
} from '../../embed/data/moderationEmbeds.js';
import { createEmbed } from '../../embed/embed.js';
import { createDBReport, updateModeration } from './moderate.helper.js';
import { Moderation, ModerationOptions } from './moderate.types.js';

export class NPLAYModeration {
	public _report: Moderation | undefined;

	constructor(
		private _data: ModerationOptions,
		private _reason?: string
	) {}

	get report() {
		if (!this._report) throw new Error('Report not initialized yet');
		return this._report;
	}

	get member(): Promise<GuildMember | null> {
		return NPLAYModerationBot.Client.guilds.fetch(this.report.guildId).then(async (guild) => {
			try {
				return await guild.members.fetch(this.report.userId);
			} catch {
				return null;
			}
		});
	}

	/**
	 * Creates a new moderation in the database.
	 * Needs to be called before `execute` as initialization.
	 */
	public async create() {
		this._report = await createDBReport(this._data, this._reason);
	}

	/**
	 * Executes the moderation action.
	 */
	public async execute() {
		switch (this.report.action) {
			case ModerationAction.WARN:
				await this.warnMember();
				break;
			case ModerationAction.TIMEOUT:
				await this.timeoutMember();
				break;
			case ModerationAction.KICK:
				await this.kickMember();
				break;
			case ModerationAction.TEMP_BAN:
				await this.banMember();
				break;
			case ModerationAction.BAN:
				await this.banMember();
				break;
		}

		this._report = await updateModeration(this.report.id, {
			status:
				this.report.action === ModerationAction.TEMP_BAN ? ReportStatus.EXECUTED : ReportStatus.DONE
		});
	}

	/**
	 * Reverts the moderation, marks it as reverted in the database and sends a message to the reported user.
	 * @param reverterId The id of the user who reverted the moderation.
	 * @param preventModlogRemoval Whether the modlog entry should be removed or not. Default: false
	 */
	public async revert(reverterId: string, preventModlogRemoval = false) {
		if (!preventModlogRemoval) {
			this._report = await updateModeration(this.report.id, { status: ReportStatus.REVERTED });
		}

		async function revertBan(nplayModeration: NPLAYModeration) {
			const guild = await NPLAYModerationBot.Client.guilds.fetch(nplayModeration.report.guildId);
			await guild.bans.remove(nplayModeration.report.userId);
		}

		switch (this.report.action) {
			case ModerationAction.TIMEOUT:
				const member = await this.member;
				if (member) {
					await member.timeout(null);
				}
				break;

			case ModerationAction.TEMP_BAN:
				await revertBan(this);
				if (preventModlogRemoval) {
					this._report = await updateModeration(this.report.id, { status: ReportStatus.DONE });
				}
				break;

			case ModerationAction.BAN:
				await revertBan(this);
				break;
		}

		const guild = await NPLAYModerationBot.Client.guilds.fetch(this.report.guildId).catch(() => {
			console.error(`Could not fetch guild ${this.report.guildId}`);
		});

		const guildName = guild ? guild.name : 'Unbekannt';

		await NPLAYModerationBot.Client.users.fetch(this.report.userId).then(async (user) => {
			await user
				.send({
					embeds: [createEmbed(RevertEmbed(this.report, guildName, reverterId))]
				})
				.catch(() => {
					console.error(`Could not send revert message to ${user.username}`);
				});
		});
	}

	private async warnMember() {
		const member = await this.member;
		if (!member) return;

		member
			.send({
				embeds: [createEmbed(WarnEmbed(this.report, member.guild.name))]
			})
			.catch(() => {
				console.error(`Could not send warn message to ${member.displayName}`);
			});
	}

	private async timeoutMember() {
		const member = await this.member;
		if (!member) return;

		if (!this.report.duration) {
			throw new Error('Der Parameter "duration" ist nicht gesetzt, wird aber benötigt.');
		}

		if (this.report.duration.getTime() - this.report.createdAt.getTime() > 2419200000) {
			throw new Error('Die maximale Dauer für einen Timeout beträgt 28 Tage.');
		}

		await member.timeout(
			this.report.duration.getTime() - this.report.createdAt.getTime(),
			this.report.reason || 'Kein Grund angegeben'
		);

		member
			.send({
				embeds: [createEmbed(TimeoutEmbed(this.report, member.guild.name))]
			})
			.catch(() => {
				console.error(`Could not send timeout message to ${member.displayName}`);
			});
	}

	private async kickMember() {
		const member = await this.member;
		if (!member) return;

		// IMPORTANT: Send the message before kicking the member!!
		member
			.send({
				embeds: [createEmbed(KickEmbed(this.report, member.guild.name))]
			})
			.catch(() => {
				console.error(`Could not send kick message to ${member.displayName}`);
			})
			.finally(async () => {
				await member.kick(this.report.reason || 'Kein Grund angegeben');
			});
	}

	private async banMember() {
		const guild = await NPLAYModerationBot.Client.guilds.fetch(this.report.guildId);
		const member = await guild.members.fetch(this.report.userId);

		const embed = this.report.duration
			? TempBanEmbed(this.report, guild.name)
			: BanEmbed(this.report, guild.name);

		// IMPORTANT: Send the message before banning the member!!
		member
			.send({
				embeds: [createEmbed(embed)]
			})
			.catch(() => {
				console.error(`Could not send (temp)ban message to ${member.displayName}`);
			})
			.finally(async () => {
				await member.ban({
					deleteMessageSeconds: this.report.delDays ? this.report.delDays * 86400 : undefined, // 86400 seconds = 1 day
					reason: this.report.reason || 'Kein Grund angegeben'
				});
			});
	}

	/**
	 * Creates a new NPLAYModeration instance from a Report object.
	 * @param moderation The moderation object to create the instance from.
	 */
	public static fromReport(moderation: Moderation) {
		const data: ModerationOptions = {
			type: moderation.action,
			reportedUserId: moderation.userId,
			issuerId: moderation.issuerId,
			paragraph: moderation.paragraph,
			guildId: moderation.guildId,
			duration: moderation.duration,
			delDays: moderation.delDays,
			message: moderation.message
		};

		const nplayModeration = new NPLAYModeration(data, moderation.reason || undefined);
		nplayModeration._report = moderation;

		return nplayModeration;
	}
}
