import {
	ApplicationCommandData,
	ApplicationCommandOption, ApplicationCommandOptionData,
	ApplicationCommandOptionType,
	CommandInteraction
} from 'discord.js';
import NPLAYInteraction from './NPLAYInteraction.js';
import { NPLAYModerationBot } from './bot.js';

export interface NPLAYCommandData {
	name: string;
	description: string;
	type?: number;
	options: ApplicationCommandOption[];
	group?: string;
}

export default abstract class NPLAYCommand extends NPLAYInteraction {
	public type: number | undefined;
	public options: ApplicationCommandOption[];

	constructor(
		public data: Omit<NPLAYCommandData, 'options'> & Partial<Pick<NPLAYCommandData, 'options'>>,
		skipRegister = false
	) {
		super(data.name, data.description);
		this.type = data.type;
		this.options = data.options || [];

		if (!skipRegister) NPLAYModerationBot.interactions.register(this);
	}

	abstract execute(interaction: CommandInteraction): Promise<void>;
	
	public toApplicationData(): ApplicationCommandData {
		return {
			name: this.data.name,
			description: this.data.description
		}
	}
	
	public toApplicationOptionData(): ApplicationCommandOptionData {
		return {
			name: this.data.name,
			description: this.data.description,
			type: 1
		}
	}
}
