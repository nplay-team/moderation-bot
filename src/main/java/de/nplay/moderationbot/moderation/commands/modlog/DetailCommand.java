package de.nplay.moderationbot.moderation.commands.modlog;

import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
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
