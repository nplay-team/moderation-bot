package de.nplay.moderationbot.moderation.commands.modlog;

import de.nplay.moderationbot.Replies;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("revert")
@Interaction
public class DeleteCommand {

    private static final Logger log = LoggerFactory.getLogger(DeleteCommand.class);
    private final Serverlog serverlog;
    private final ModerationActService actService;

    @Inject
    public DeleteCommand(Serverlog serverlog, ModerationActService actService) {
        this.serverlog = serverlog;
        this.actService = actService;
    }

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command(value = "mod delete")
    @Permissions(BotPermissions.MODERATION_DELETE)
    public void deleteModeration(CommandEvent event, @Param(type = OptionType.NUMBER) ModerationAct moderationAct) {
        RevertedModerationAct reverted = actService.revert(moderationAct, event, event.resolve("delete-reason"));
        log.info("Moderation act {} has been deleted by {}", moderationAct.id(), event.getUser().getName());
        actService.delete(moderationAct.id());
        serverlog.onEvent(ModerationEvents.Deleted(event.getJDA(), event.getGuild(), reverted), event);
        event.reply(Replies.success("delete-successful"), entry("id", moderationAct.id()));
    }

}
