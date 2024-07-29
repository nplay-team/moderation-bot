import { ReportAction, ReportStatus } from '@prisma/client';
import { CronJob } from 'cron';
import { NPLAYModerationBot } from '../bot.js';
import { NPLAYReport } from '../modules/report/NPLAYReport.js';
import { Report } from '../modules/report/report.types.js';

/**
 * This task will run hourly to check if any temporary bans have expired.
 * If so, it will unban the user.
 */
new CronJob('0 0 * * * *', async () => {
	const bans: Report[] = await NPLAYModerationBot.db.report.findMany({
		where: {
			action: ReportAction.TEMP_BAN,
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
			await NPLAYReport.fromReport(ban).revert(NPLAYModerationBot.Client.user.id, true);
		}
	}
}).start();
