package de.nplay.moderationbot.tasks;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record AutomaticRevertTask(Guild guild, EmbedCache embedCache, User bot) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(AutomaticRevertTask.class);

    @Override
    public void run() {
        ModerationService.getModerationActsToRevert().forEach(it -> it.revert(guild, embedCache, bot, "Automatische Aufhebung nach Ablauf der Dauer"));
    }
}
