package de.nplay.moderationbot.moderation.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
public class DetailCommand {

    @Command(value = "mod detail", desc = "Zeigt mehr Informationen zu einer Moderationshandlung an")
    @Permissions(BotPermissions.MODERATION_READ)
    public void detail(CommandEvent event, @Param("Die ID der Moderationshandlung") Long moderationId) {
        var moderationAct = ModerationService.getModerationAct(moderationId);

        if (moderationAct.isEmpty()) {
            event.with().embeds("moderationActNotFound", entry("id", moderationId));
            return;
        }

        event.reply(moderationAct.get().getEmbed(event, event.getJDA(), event.getGuild()).build());
    }
}
