import { Glob } from 'glob';

export async function importCommands(path: string) {
	const g = new Glob(path, {});

	for await (const file of g) {
		await import(file);
		console.log(`Import command: ${file}`);
	}
}
