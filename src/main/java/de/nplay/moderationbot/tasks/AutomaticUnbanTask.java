package de.nplay.moderationbot.tasks;

import de.nplay.moderationbot.moderation.ModerationService;

import java.util.TimerTask;

public class AutomaticUnbanTask extends TimerTask {

    @Override
    public void run() {
        ModerationService.getModerationActsToRevert().forEach(ModerationService::revertModerationAct);
    }
}
