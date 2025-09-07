package de.nplay.moderationbot.moderation.commands.modlog;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@Interaction
public class DetailCommand {

    @Command("mod detail")
    @Permissions(BotPermissions.MODERATION_READ)
    public void detail(CommandEvent event, @Param(type = OptionType.NUMBER) ModerationAct moderationAct) {
        event.with().embeds(moderationAct.toEmbed(event)).reply();
    }

}
