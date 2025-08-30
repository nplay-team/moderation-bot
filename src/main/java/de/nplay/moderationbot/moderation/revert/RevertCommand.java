package de.nplay.moderationbot.moderation.revert;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
public class RevertCommand {

    @Inject
    private Serverlog serverlog;

    @Command(value = "mod revert", desc = "Hebt eine Moderationshandlung auf")
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void revertModeration(CommandEvent event, @Param("Die ID der Moderationshandlung, die aufgehoben werden soll") long moderationId,
                                 @Param(value = "Der Grund für die Aufhebung", optional = true) String reason) {
        var moderation = ModerationService.getModerationAct(moderationId);

        if (moderation.isEmpty() || moderation.get().reverted()) {
            event.with().embeds("reversionFailed", entry("id", moderationId)).reply();
            return;
        }

        moderation.get().revert(event.getGuild(), event::embed, event.getUser(), reason);
        var revertedModeration = ModerationService.getModerationAct(moderationId);
        revertedModeration.ifPresent(it -> serverlog.onEvent(ModerationEvents.Reverted(event.getJDA(), event.getGuild(), it), event));
        event.with().embeds("reversionSuccessful", entry("id", moderationId)).reply();
    }

}
