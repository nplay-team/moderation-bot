package de.nplay.moderationbot.moderation.commands.create;

import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class KickCommands extends CreateCommands {

    @Inject
    public KickCommands(ModerationActLock moderationActLock, Serverlog serverlog) {
        super(moderationActLock, serverlog);
    }

    @CommandConfig(enabledFor = Permission.KICK_MEMBERS)
    @Command("mod kick")
    public void kickMember(CommandEvent event,
                           Member target,
                           @Param(optional = true) String paragraph,
                           @Param(optional = true) @Max(7) int delDays,
                           @Param(optional = true) MessageLink messageLink) {
        if (moderationActLock.checkLocked(event, target, event.getUser())) {
            return;
        }
        moderationActBuilder = ModerationActBuilder.kick(target, event.getUser()).paragraph(paragraph).
                deletionDays(delDays)
                .messageReference(Helpers.retrieveMessage(event, messageLink));

        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Kick)"));
    }

    @Modal(value = "reason-title")
    public void onModerate(ModalEvent event, @TextInput("reason-field") String reason) {
        executeModeration(event, reason);
    }
}
