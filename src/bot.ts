import { NotBot } from '@discordx/utilities';
import { PrismaClient } from '@prisma/client';
import { ActivityType, IntentsBitField, Partials } from 'discord.js';
import { Client } from 'discordx';
import 'dotenv/config';
import { dirname, importCommands } from './command.importer.js';
import './tasks/autoBanRevert.js';

export class NPLAYModerationBot {
	private static _client: Client;
	private static _prismaClient: PrismaClient;

	/**
	 * The discord.js client
	 */
	static get Client(): Client {
		return this._client;
	}

	/**
	 * The Prisma client
	 */
	static get db(): PrismaClient {
		return this._prismaClient;
	}

	/**
	 * Initialize the Prisma client
	 */
	static initDB(): void {
		this._prismaClient = new PrismaClient();
	}

	/**
	 * Start the bot
	 */
	static async start(): Promise<void> {
		const guildID = process.env.GUILD_ID;

		if (!guildID) {
			console.warn(
				'GUILD_ID not found in environment variables, bot will fall back to global modules'
			);
		}

		this._client = new Client({
			intents: [
				IntentsBitField.Flags.Guilds,
				IntentsBitField.Flags.GuildMessages,
				IntentsBitField.Flags.DirectMessages,
				IntentsBitField.Flags.MessageContent
			],
			partials: [Partials.Channel, Partials.Message],
			silent: process.env.NODE_ENV === 'production',
			botGuilds: guildID ? [guildID] : undefined,
			guards: [NotBot]
		});

		this._client.once('ready', async () => {
			await this._client.initApplicationCommands();

			if (this._client.user) {
				this._client.user.setPresence({
					status: 'online',
					activities: [
						{
							name: 'euren Nachrichten',
							type: ActivityType.Listening
						}
					]
				});
			}

			console.log('Bot started');
		});

		this._client.on('interactionCreate', (interaction) => {
			this._client.executeInteraction(interaction);
		});

		await importCommands(`${dirname(import.meta.url)}/modules/**/*.{js,ts}`);

		if (!process.env.BOT_TOKEN) {
			throw Error('Could not find BOT_TOKEN in your environment');
		}
		await this._client.login(process.env.BOT_TOKEN);
	}

	private static destroy(): void {
		this._client.user?.setPresence({ activities: [], status: 'invisible' });
		this._client.destroy();
		this._prismaClient.$disconnect();
	}

	/**
	 * Restart the bot
	 */
	static async restart(): Promise<void> {
		console.log('Restarting bot...');
		this.destroy();
		process.exit(1);
	}

	/**
	 * Stop the bot
	 */
	static async stop(): Promise<void> {
		console.log('Stopping bot...');
		this.destroy();
		process.exit(0);
	}
}

NPLAYModerationBot.initDB();
NPLAYModerationBot.start();
