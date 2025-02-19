package de.nplay.moderationbot.moderation.modlog;

import de.nplay.moderationbot.BotEvent;
import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

public class GenericModerationEvent extends BotEvent {

    private final ModerationService.ModerationAct moderationAct;

    public GenericModerationEvent(@NotNull JDA api, @NotNull Guild guild, @NotNull ModerationService.ModerationAct moderationAct) {
        super(api, guild);
        this.moderationAct = moderationAct;
    }

    public ModerationService.ModerationAct getModerationAct() {
        return moderationAct;
    }
}
