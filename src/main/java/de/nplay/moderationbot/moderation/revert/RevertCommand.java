package de.nplay.moderationbot.moderation.revert;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.moderation.modlog.GenericModerationEvent;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.Serverlog;
import de.nplay.moderationbot.serverlog.events.ServerlogEvents;
import net.dv8tion.jda.api.Permission;

@Interaction
public class RevertCommand {
    
    @Inject
    private EmbedCache embedCache;

    @Inject
    private Serverlog serverlog;

    @SlashCommand(value = "moderation revert", desc = "Hebt eine Moderationshandlung auf", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void revertModeration(CommandEvent event, @Param("Die ID der Moderationshandlung, die aufgehoben werden soll") long moderationId,
                                 @Optional @Param(value = "Der Grund für die Aufhebung") String reason) {
        var moderation = ModerationService.getModerationAct(moderationId);

        if (moderation.isEmpty() || moderation.get().reverted()) {
            event.reply(embedCache.getEmbed("reversionFailed").injectValue("id", moderationId).injectValue("color", EmbedColors.ERROR));
            return;
        }

        moderation.get().revert(event.getGuild(), embedCache, event.getUser(), reason);
        var revertedModeration = ModerationService.getModerationAct(moderationId);
        revertedModeration.ifPresent(it -> serverlog.trigger(ServerlogEvents.MODERATION_REVERTED, new GenericModerationEvent(event.getJDA(), event.getGuild(), it)));
        event.reply(embedCache.getEmbed("reversionSuccessful").injectValue("id", moderationId).injectValue("color", EmbedColors.SUCCESS));
    }

}
