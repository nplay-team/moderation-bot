import { Glob } from 'glob';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

/**
 * Import all modules from a path
 * @param path The path to import the modules from, needs to be a glob pattern to match the files
 * @example
 * importCommands('src/modules/*.ts');
 */
export async function importCommands(path: string) {
	const g = new Glob(path, {});

	for await (const file of g) {
		await import(file);
		console.log(`Import command: ${file}`);
	}
}

/**
 * Get the directory name of a file
 * @param url The URL of the file
 */
export function dirname(url: string): string {
	return path.dirname(fileURLToPath(url));
}
