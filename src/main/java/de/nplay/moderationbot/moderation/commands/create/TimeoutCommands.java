package de.nplay.moderationbot.moderation.commands.create;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.duration.DurationMax;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.moderation.act.ModerationActBuilder;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

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

    @Command(value = "Timeoute Mitglied", type = Type.USER)
    public void timeoutMemberContext(CommandEvent event, Member target) {
        if (moderationActLock.checkLocked(event, target, event.getUser())) {
            return;
        }
        moderationActBuilder = ModerationActBuilder.timeout(target, event.getUser());
        replyEphemeral = true;
        event.replyModal("onModerateDuration", modal -> modal.title("Begründung und Dauer angeben (Timeout)"));
    }

    @Command(value = "Timeoute Mitglied (💬)", type = Type.MESSAGE)
    public void timeoutMemberMessageContext(CommandEvent event, Message target) {
        if (moderationActLock.checkLocked(event, target.getAuthor(), event.getUser())) {
            return;
        }
        moderationActBuilder = ModerationActBuilder.timeout(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        event.replyModal("onModerateDuration", modal -> modal.title("Begründung und Dauer angeben (Timeout)"));
    }

    @Modal(value = "reason-duration-title")
    public void onModerateDuration(ModalEvent event,
                                   @TextInput(value = "reason-field") String reason,
                                   @TextInput(value = "duration-field", style = TextInputStyle.SHORT)
                                   String until) {
        var duration = durationAdapter.parse(until);
        if (duration.isEmpty()) {
            event.with().ephemeral(true).reply("invalid-duration");
            return;
        }
        if (duration.get().getSeconds() > 2419200) {
            event.with().ephemeral(true).reply("invalid-duration-limit");
            return;
        }

        moderationActBuilder.duration(duration.get()).reason(reason).execute(event);
        onModerate(event, reason);
    }

    @Modal(value = "reason-title")
    public void onModerate(ModalEvent event, @TextInput("reason-field") String reason) {
        if (ModerationService.isTimeOuted(moderationActBuilder.targetId())) {
            event.with().embeds("userAlreadyTimeOuted").reply();
            return;
        }

        executeModeration(event, reason);
    }
}
