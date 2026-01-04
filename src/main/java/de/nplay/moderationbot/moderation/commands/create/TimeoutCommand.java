package de.nplay.moderationbot.moderation.commands.create;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.duration.DurationMax;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class TimeoutCommand extends CreateCommand {

    @Inject
    public TimeoutCommand(ModerationActLock moderationActLock) {
        super(moderationActLock);
    }

    @Command("mod timeout")
    public void timeoutMember(CommandEvent event,
                              Member target,
                              @DurationMax(amount = 28, unit = ChronoUnit.DAYS) Duration until,
                              @Param(optional = true) String paragraph,
                              @Param(optional = true) MessageLink messageLink) {
        if (checkLocked(event, target, event.getUser())) {
            return;
        }

        if (ModerationActService.isTimeOuted(target.getIdLong())) {
            event.with().embeds("userAlreadyTimeOuted").reply();
            return;
        }

        event.kv().put(BUILDER, ModerationActBuilder.timeout(target, event.getUser())
                .duration(until)
                .paragraph(paragraph)
                .messageReference(Helpers.retrieveMessage(event, messageLink)));

        replyModal(event, "Timeout");
    }
}
