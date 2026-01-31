package de.nplay.moderationbot.moderation.commands.modlog;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.lifecycle.events.ModerationEvent;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("revert")
@Interaction
public class RevertCommand {

    private final ModerationActService actService;

    @Inject
    public RevertCommand(ModerationActService actService) {
        this.actService = actService;
    }

    @Command("mod revert")
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void revertModeration(CommandEvent event, @Param(type = OptionType.NUMBER) ModerationAct moderationAct, String reason) {
        if (moderationAct instanceof RevertedModerationAct) {
            event.reply(Replies.error("revert-failed"), entry("id", moderationAct.id()));
            return;
        }
        RevertedModerationAct reverted = actService.revert(moderationAct, event, reason);
        actService.publish(new ModerationEvent.Revert(reverted, false));
        event.reply(Replies.success("revert-successful"), entry("id", moderationAct.id()));
    }
}
