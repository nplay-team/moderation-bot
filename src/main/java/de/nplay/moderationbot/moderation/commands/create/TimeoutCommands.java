package de.nplay.moderationbot.moderation.commands.create;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.duration.DurationMax;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class TimeoutCommands extends CreateCommands {

    @Inject
    public TimeoutCommands(ModerationActLock moderationActLock, Serverlog serverlog) {
        super(moderationActLock, serverlog);
    }

    @Command("mod timeout")
    public void timeoutMember(CommandEvent event,
                              Member target,
                              @DurationMax(amount = 28, unit = ChronoUnit.DAYS) Duration until,
                              @Param(optional = true) String paragraph,
                              @Param(optional = true) MessageLink messageLink) {
        if (moderationActLock.checkLocked(event, target, event.getUser())) {
            return;
        }
        moderationActBuilder = ModerationActBuilder.timeout(target, event.getUser())
                .duration(until)
                .paragraph(paragraph)
                .messageReference(Helpers.retrieveMessage(event, messageLink));
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Timeout)"));
    }

    @Modal(value = "reason-title")
    public void onModerate(ModalEvent event, @TextInput("reason-field") String reason) {
        if (ModerationActService.isTimeOuted(moderationActBuilder.targetId())) {
            event.with().embeds("userAlreadyTimeOuted").reply();
            return;
        }

        executeModeration(event, reason);
    }
}
