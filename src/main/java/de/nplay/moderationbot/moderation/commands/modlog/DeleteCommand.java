package de.nplay.moderationbot.moderation.commands.modlog;

import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.lifecycle.events.ModerationEvent;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("revert")
@Interaction
public class DeleteCommand {

    private final ModerationActService actService;

    @Inject
    public DeleteCommand(ModerationActService actService) {
        this.actService = actService;
    }

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command(value = "mod delete")
    @Permissions(BotPermissions.MODERATION_DELETE)
    public void deleteModeration(CommandEvent event, @Param(type = OptionType.NUMBER) ModerationAct moderationAct) {
        RevertedModerationAct reverted = actService.revert(moderationAct, event, event.resolve("delete-reason"));
        actService.delete(moderationAct.id());
        actService.publish(new ModerationEvent.Delete(moderationAct, event.getUser()));
        event.reply(Replies.success("delete-successful"), entry("id", moderationAct.id()));
    }

}
