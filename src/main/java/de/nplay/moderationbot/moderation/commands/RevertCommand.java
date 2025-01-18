package de.nplay.moderationbot.moderation.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;

@Interaction
public class RevertCommand {

    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "moderation revert", desc = "Hebt eine Moderationshandlung auf", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(BotPermissions.MODERATION_DELETE)
    public void revertModeration(CommandEvent event, @Param("Die ID der Moderationshandlung, die aufgehoben werden soll") long moderationId) {
        var moderation = ModerationService.getModerationAct(moderationId);

        if (moderation == null) {
            event.reply(embedCache.getEmbed("reversionFailed").injectValue("id", moderationId).injectValue("color", EmbedColors.ERROR));
            return;
        }

        ModerationService.revertModerationAct(moderation);
        event.reply(embedCache.getEmbed("reversionSuccessful").injectValue("id", moderationId).injectValue("color", EmbedColors.SUCCESS));
    }

}
