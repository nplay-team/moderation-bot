import { ModerationAction, ReportStatus } from '@prisma/client';
import { CronJob } from 'cron';
import { NPLAYModerationBot } from '../bot.js';
import { NPLAYModeration } from '../modules/moderation/NPLAYModeration.js';
import { Moderation } from '../modules/moderation/moderate.types.js';

/**
 * This task will run hourly to check if any temporary bans have expired.
 * If so, it will unban the user.
 */
new CronJob('0 0 * * * *', async () => {
	const bans: Moderation[] = await NPLAYModerationBot.db.moderation.findMany({
		where: {
			action: ModerationAction.TEMP_BAN,
			duration: {
				lte: new Date()
			},
			status: ReportStatus.EXECUTED
		},
		include: {
			paragraph: true
		}
	});

	if (NPLAYModerationBot.Client.user !== null) {
		for (const ban of bans) {
			await NPLAYModeration.fromReport(ban).revert(NPLAYModerationBot.Client.user.id, true);
		}
	}
}).start();
