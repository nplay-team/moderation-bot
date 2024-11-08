import { CommandData, CommandOptions, SlashCommandProps } from 'commandkit';

export const data: CommandData = {
	name: 'test',
	description: 'Testet das Command System!',
}

export function run({ interaction, client, handler }: SlashCommandProps) {
	interaction.reply('Test command executed!');
}

export const options: CommandOptions = {
	botPermissions: ['Administrator', 'AddReactions'],
	deleted: false,
}
