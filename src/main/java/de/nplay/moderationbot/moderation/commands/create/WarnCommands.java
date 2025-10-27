package de.nplay.moderationbot.moderation.commands.create;

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
import net.dv8tion.jda.api.entities.Member;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class WarnCommands extends CreateCommands {

    @Inject
    public WarnCommands(ModerationActLock moderationActLock, Serverlog serverlog) {
        super(moderationActLock, serverlog);
    }

    @Command("mod warn")
    public void warnMember(CommandEvent event, Member target,
                           @Param(optional = true) String paragraph, @Param(optional = true) MessageLink messageLink) {
        if (moderationActLock.checkLocked(event, target, event.getUser())) {
            return;
        }
        this.moderationActBuilder = ModerationActBuilder.warn(target, event.getUser())
                .paragraph(paragraph)
                .messageReference(Helpers.retrieveMessage(event, messageLink));
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Warn)"));
    }

    @Modal(value = "reason-title")
    public void onModerate(ModalEvent event, @TextInput("reason-field") String reason) {
        executeModeration(event, reason);
    }
}
