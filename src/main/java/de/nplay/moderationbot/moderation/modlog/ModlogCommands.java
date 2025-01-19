package de.nplay.moderationbot.moderation.modlog;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.SlashCommand;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.permissions.BotPermissionFlags;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@Interaction
@Permissions(BotPermissionFlags.MODLOG_READ)
public class ModlogCommands {

    @Inject
    EmbedCache embedCache;
    
    @SlashCommand(value = "moderation modlog", desc = "Zeigt den Modlog eines Mitglieds an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    public void modlog(CommandEvent event, @Param("Der Member, dessen Modlog abgerufen werden soll") Member member) {
        var moderationActs = ModerationService.getModerationActs()
                .stream()
                .filter(act -> act.userId() == Long.parseLong(member.getId()))
                .toList();

        event.reply(ModlogService.getModlogEmbedHeader(embedCache, member));
    }

}
