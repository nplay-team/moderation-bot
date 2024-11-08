import { PrismaClient } from '@prisma/client';
import { ActivityType, IntentsBitField, Partials, Client } from 'discord.js';
import 'dotenv/config';
import NPLAYBotInteractionHandler from './NPLAYBotInteractionHandler.js';
import './tasks/autoBanRevert.js';
import { CommandKit } from 'commandkit';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export class NPLAYModerationBot {
	public static guildID = process.env.GUILD_ID;

	private static _client: Client = new Client({
		intents: [
			IntentsBitField.Flags.Guilds,
			IntentsBitField.Flags.GuildMessages,
			IntentsBitField.Flags.DirectMessages,
			IntentsBitField.Flags.MessageContent
		],
		partials: [Partials.Channel, Partials.Message]
	});
	
	private static _commandKit: CommandKit = new CommandKit({
		client: this._client,
		commandsPath: path.join(path.dirname(fileURLToPath(import.meta.url)), 'commands'),
		eventsPath: path.join(path.dirname(fileURLToPath(import.meta.url)), 'events'),
		validationsPath: path.join(path.dirname(fileURLToPath(import.meta.url)), 'validations'),
		devGuildIds: this.guildID ? [this.guildID] : undefined,
		skipBuiltInValidations: true,
		bulkRegister: true,
	});


	private static _prismaClient: PrismaClient;

	public static interactions: NPLAYBotInteractionHandler = new NPLAYBotInteractionHandler();

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
		if (!this.guildID) {
			console.warn(
				'GUILD_ID not found in environment variables, bot will fall back to global modules'
			);
		}

		this._client.once('ready', async () => {
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
			this.interactions.handleInteraction(interaction)
		});

		//await this.interactions.importCommands();
		
		if (!process.env.BOT_TOKEN) {
			throw Error('Could not find BOT_TOKEN in your environment');
		}

		await this._client.login(process.env.BOT_TOKEN).then(() => {
			//this.interactions.registerCommands();
		});
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
