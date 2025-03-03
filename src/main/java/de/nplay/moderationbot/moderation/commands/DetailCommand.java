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
public class DetailCommand {

    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "moderation detail", desc = "Zeigt mehr Informationen zu einer Moderationshandlung an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
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
