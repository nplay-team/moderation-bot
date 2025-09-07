package de.nplay.moderationbot.moderation.commands.revert;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
public class RevertCommand {

    @Inject
    private Serverlog serverlog;

    @Command("mod revert")
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void revertModeration(CommandEvent event,
                                 @Param(type = OptionType.NUMBER) ModerationAct moderationAct,
                                 @Param(optional = true) String reason) {
        ModerationAct reverted = moderationAct.revert(event.getGuild(), event::embed, event.getUser(), reason);
        serverlog.onEvent(ModerationEvents.Reverted(event.getJDA(), event.getGuild(), reverted), event);
        event.with().embeds("reversionSuccessful", entry("id", moderationAct.id())).reply();
    }
}
