package de.nplay.moderationbot.moderation.events;

import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class ModerationDeleteEvent extends GenericModerationEvent {
    private User deleter;

    public ModerationDeleteEvent(@NotNull JDA api, @NotNull Guild guild, ModerationService.@NotNull ModerationAct moderationAct, @NotNull User deleter) {
        super(api, guild, moderationAct);
        this.deleter = deleter;
    }

    public User getDeleter() {
        return deleter;
    }
}
