package de.nplay.moderationbot.tasks;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.entities.Guild;

public record AutomaticUnbanTask(Guild guild, EmbedCache embedCache) implements Runnable {

    @Override
    public void run() {
        ModerationService.getModerationActsToRevert().forEach(it -> it.revert(guild, embedCache));
    }
}
