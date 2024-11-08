import { ApplicationCommandData, Interaction } from 'discord.js';
import { NPLAYModerationBot } from './bot.js';
import NPLAYCommand from './NPLAYCommand.js';
import NPLAYInteraction from './NPLAYInteraction.js';
import { Glob } from 'glob';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export default class NPLAYBotInteractionHandler {
	private _interactions: Map<string, NPLAYInteraction> = new Map();
	private _commands: Map<string,  NPLAYCommand[]> = new Map();

	constructor() {
		NPLAYModerationBot.Client.on('interactionCreate', async (interaction) => {
			if (!interaction.isCommand()) return; // TODO: Allow support for other interactions
			const command = this.get(interaction.commandName);
			if (!command) return;
			if (command instanceof NPLAYCommand) await command.execute(interaction);
		});
	}

	public register(interaction: NPLAYInteraction) {
		this._interactions.set(interaction.name, interaction);
	}

	public get(name: string): NPLAYInteraction | undefined {
		return this._interactions.get(name);
	}

	public get commandJson() {
		const commandMap: ApplicationCommandData[] = [];

		this._commands.forEach((commands, group) => {
			if (group === "_") {
				commandMap.push(...commands.map(c => c.toApplicationData()))
			} else {
				commandMap.push({
					name: group,
					description: `Auto-generated group ${group}`,
					options: commands.map(c => c.toApplicationOptionData())
				})
			}
		})

		return commandMap;
	}
	
	public async importCommands() {
		console.log("----------------------------------")
		console.log("[/] Importing Commands");

		const commands: Map<string, NPLAYCommand[]> = new Map<string, NPLAYCommand[]>();
		commands.set("_", [])

		const g = new Glob(path.join(path.dirname(fileURLToPath(import.meta.url)), 'commands/**/*.{js,ts}'), {});
		
		for (const file of g) {
			const commandModule = await import(file);
			const command = commandModule.default;

			if (!command) {
				throw Error(`Failed importing command: ${file}`)
			}
			
			console.log(`[/] Imported command: ${file}`);
			
			if (command.data.group && !commands.get(command.data.group)) {
				commands.set(command.data.group, [])
			} 
			
			if (command.data.group) {
				commands.set(command.data.group, [command, ...commands.get(command.data.group)!])
			} else {
				commands.set("_", [command, ...commands.get("_")!])
			}
		}

		this._commands = commands;	
	}

	public async registerCommands() {
		console.log('----------------------------------');
		console.log('[/] Registering commands');

		if (NPLAYModerationBot.guildID) {
			console.log('[/] Registering guild commands');
			NPLAYModerationBot.Client.guilds.fetch(NPLAYModerationBot.guildID).then(async (guild) => {
				await guild.commands
					.set(this.commandJson)
					.then(() => {
						console.log('[/] Guild commands registered');
					})
					.catch((error) => {
						console.error('[/] Failed to register guild commands:', error);
					});
			});
		} else {
			await NPLAYModerationBot.Client.application?.commands
				.set(this.commandJson)
				.then(() => {
					console.log('[/] Global commands registered');
				})
				.catch((error) => {
					console.error('[/] Failed to register commands:', error);
				});
		}
	}
	
	public async handleInteraction(interaction: Interaction) {
		if (interaction.isCommand()) {
			console.log(`[»] ${interaction.commandName} - ${interaction} - ${interaction.user.username} - ${new Date().toISOString()}`);
			console.log(interaction.options.data);
		}
	}
}
