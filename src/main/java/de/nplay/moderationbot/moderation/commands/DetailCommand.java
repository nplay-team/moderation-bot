package de.nplay.moderationbot.moderation.commands;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.moderation.ModerationService.ModerationAct;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
public class DetailCommand {

    @Command(value = "mod detail", desc = "Zeigt mehr Informationen zu einer Moderationshandlung an")
    @Permissions(BotPermissions.MODERATION_READ)
    public void detail(CommandEvent event, @Param(value = "detail-act", type = OptionType.NUMBER) ModerationAct moderationAct) {
        event.with().embeds(moderationAct.getEmbed(event, event.getJDA(), event.getGuild())).reply();
    }

}
