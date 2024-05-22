import { dirname, importx } from '@discordx/importer';
import { PrismaClient } from '@prisma/client';
import { IntentsBitField, Partials } from 'discord.js';
import { Client } from 'discordx';
import 'dotenv/config';

export class NPLAYModerationBot {
	private static _client: Client;
	private static _prismaClient: PrismaClient;

	static get Client(): Client {
		return this._client;
	}

	static get db(): PrismaClient {
		return this._prismaClient;
	}

	static initDB(): void {
		this._prismaClient = new PrismaClient();
	}

	static async start(): Promise<void> {
		const guildID = process.env.GUILD_ID;

		if (!guildID) {
			console.warn(
				'GUILD_ID not found in environment variables, bot will fall back to global commands'
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
			botGuilds: guildID ? [guildID] : undefined
		});

		this._client.once('ready', async () => {
			await this._client.initApplicationCommands();

			console.log('Bot started');
		});

		this._client.on('interactionCreate', (interaction) => {
			this._client.executeInteraction(interaction);
		});

		await importx(`${dirname(import.meta.url)}/commands/**/*.{js,ts}`);

		if (!process.env.BOT_TOKEN) {
			throw Error('Could not find BOT_TOKEN in your environment');
		}
		await this._client.login(process.env.BOT_TOKEN);
	}
}

NPLAYModerationBot.initDB();
NPLAYModerationBot.start();
