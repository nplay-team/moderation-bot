package de.nplay.moderationbot.moderation.commands.modlog;

import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Interaction
public class RevertCommand {

    @Inject
    private Serverlog serverlog;

    @Command("mod revert")
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void revertModeration(CommandEvent event, @Param(type = OptionType.NUMBER) ModerationAct moderationAct, String reason) {
        if (moderationAct instanceof RevertedModerationAct) {
            event.with().embeds("reversionFailed", entry("id", moderationAct.id())).reply();
            return;
        }
        RevertedModerationAct reverted = moderationAct.revert(event.getGuild(), event::embed, event.getUser(), reason);
        serverlog.onEvent(ModerationEvents.Reverted(event.getJDA(), event.getGuild(), reverted), event);
        event.with().embeds("reversionSuccessful", entry("id", moderationAct.id())).reply();
    }
}
