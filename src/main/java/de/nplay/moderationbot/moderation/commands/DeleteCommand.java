package de.nplay.moderationbot.moderation.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
public class DeleteCommand {

    private static final Logger log = LoggerFactory.getLogger(DeleteCommand.class);

    @Inject
    private Serverlog serverlog;

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command(value = "mod delete", desc = "Löscht eine Moderationshandlung")
    @Permissions(BotPermissions.MODERATION_DELETE)
    public void deleteModeration(CommandEvent event, @Param("Die ID der Moderationshandlung, die gelöscht werden soll") long moderationId) {
        var moderation = ModerationService.getModerationAct(moderationId);

        if (moderation.isEmpty()) {
            event.with().embeds("deletionFailed", entry("id", moderationId)).reply();
            return;
        }

        if (!moderation.get().reverted()) {
            moderation.get().revert(event.getGuild(), event::embed, event.getUser(), null);
        }

        log.info("Moderation act {} has been deleted by {}", moderationId, event.getUser().getName());
        ModerationService.deleteModerationAct(moderationId);
        serverlog.onEvent(ModerationEvents.Deleted(event.getJDA(), event.getGuild(), moderation.get(), event.getUser()), event);
        event.with().embeds("deletionSuccessful", entry("id", moderationId)).reply();
    }

}
