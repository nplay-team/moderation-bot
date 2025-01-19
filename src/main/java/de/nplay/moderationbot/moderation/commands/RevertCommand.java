package de.nplay.moderationbot.moderation.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Interaction
public class RevertCommand {

    private static final Logger log = LoggerFactory.getLogger(RevertCommand.class);
    
    @Inject
    private EmbedCache embedCache;

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
        event.reply(embedCache.getEmbed("reversionSuccessful").injectValue("id", moderationId).injectValue("color", EmbedColors.SUCCESS));
    }

    @SlashCommand(value = "moderation delete", desc = "Löscht eine Moderationshandlung", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(BotPermissions.MODERATION_DELETE)
    public void deleteModeration(CommandEvent event, @Param("Die ID der Moderationshandlung, die gelöscht werden soll") long moderationId) {
        var moderation = ModerationService.getModerationAct(moderationId);

        if (moderation.isEmpty()) {
            event.reply(embedCache.getEmbed("deletionFailed").injectValue("id", moderationId).injectValue("color", EmbedColors.ERROR));
            return;
        }

        if (!moderation.get().reverted()) {
            moderation.get().revert(event.getGuild(), embedCache, event.getUser(), null);
        }
        
        log.info("Moderation act {} has been deleted by {}", moderationId, event.getUser().getName());
        ModerationService.deleteModerationAct(moderationId);
        event.reply(embedCache.getEmbed("deletionSuccessful").injectValue("id", moderationId).injectValue("color", EmbedColors.SUCCESS));
    }

}
