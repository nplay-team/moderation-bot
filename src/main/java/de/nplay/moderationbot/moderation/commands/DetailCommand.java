package de.nplay.moderationbot.moderation.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.google.inject.Inject;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;

@Interaction
public class DetailCommand {

    @Inject
    private EmbedCache embedCache;

    @Command(value = "mod detail", desc = "Zeigt mehr Informationen zu einer Moderationshandlung an")
    @Permissions(BotPermissions.MODERATION_READ)
    public void detail(CommandEvent event, @Param("Die ID der Moderationshandlung") Long moderationId) {
        var moderationAct = ModerationService.getModerationAct(moderationId);

        if (moderationAct.isEmpty()) {
            event.reply(
                    embedCache.getEmbed("moderationActNotFound")
                            .injectValue("id", moderationId)
                            .injectValue("color", EmbedColors.ERROR)
            );
            return;
        }

        event.reply(moderationAct.get().getEmbed(embedCache, event.getJDA(), event.getGuild()));
    }
}
