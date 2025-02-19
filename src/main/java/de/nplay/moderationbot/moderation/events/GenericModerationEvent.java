package de.nplay.moderationbot.moderation.events;

import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import org.jetbrains.annotations.NotNull;

public class GenericModerationEvent extends GenericGuildEvent {

    private final ModerationService.ModerationAct moderationAct;

    public GenericModerationEvent(@NotNull JDA api, @NotNull Guild guild, @NotNull ModerationService.ModerationAct moderationAct) {
        super(api, moderationAct.id(), guild);
        this.moderationAct = moderationAct;
    }

    public ModerationService.ModerationAct getModerationAct() {
        return moderationAct;
    }
}
