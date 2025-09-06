package de.nplay.moderationbot.moderation.commands.create;

import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.ModerationActBuilder;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class BanCommands extends CreateCommands {

    @Inject
    public BanCommands(ModerationActLock moderationActLock, Serverlog serverlog) {
        super(moderationActLock, serverlog);
    }

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command("mod ban")
    public void banMember(
            CommandEvent event,
            User target,
            @Param(optional = true) @Nullable Duration until,
            @Param(optional = true) @Min(1) @Max(7) int delDays,
            @Param(optional = true) @Nullable String paragraph,
            @Param(optional = true) @Nullable MessageLink messageLink
    ) {
        if (moderationActLock.checkLocked(event, target, event.getUser())) {
            return;
        }

        Member member;
        try {
            member = event.getGuild().retrieveMember(target).complete();
            moderationActBuilder = ModerationActBuilder.ban(member, event.getUser()).deletionDays(delDays);
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                moderationActBuilder = ModerationActBuilder.ban(target, event.getGuild(), event.getUser()).deletionDays(delDays);
            } else {
                throw new IllegalStateException(e);
            }
        }

        moderationActBuilder.paragraph(paragraph).messageReference(Helpers.retrieveMessage(event, messageLink));

        if (until != null) {
            moderationActBuilder.duration(until);
            event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Temp-Ban)"));
        } else {
            event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Ban)"));
        }
    }

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command(value = "(Temp-)Ban Mitglied", type = net.dv8tion.jda.api.interactions.commands.Command.Type.USER)
    public void banMemberContext(CommandEvent event, User target) {
        if (moderationActLock.checkLocked(event, target, event.getUser())) {
            return;
        }
        moderationActBuilder = ModerationActBuilder.ban(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        event.replyModal("onModerateDuration", modal -> modal.title("Begründung und Dauer angeben (Temp-Ban)"));
    }

    @CommandConfig(enabledFor = Permission.BAN_MEMBERS)
    @Command(value = "(Temp-)Ban Mitglied (💬)", type = net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE)
    public void banMemberMessageContext(CommandEvent event, Message target) {
        if (moderationActLock.checkLocked(event, target.getAuthor(), event.getUser())) {
            return;
        }
        moderationActBuilder = ModerationActBuilder.ban(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        event.replyModal("onModerateDuration", modal -> modal.title("Begründung und Dauer angeben (Temp-Ban)"));
    }

    @Modal(value = "reason-duration-title")
    public void onModerateDuration(ModalEvent event,
                                   @TextInput(value = "reason-field") String reason,
                                   @TextInput(value = "duration-field", style = TextInputStyle.SHORT, required = false)
                                   String until) {
        var duration = durationAdapter.parse(until);
        if (!until.isEmpty() && duration.isEmpty()) {
            event.with().ephemeral(true).reply("invalid-duration");
            return;
        }
        if (duration.isPresent()) {
            var seconds = duration.get().getSeconds();
            if (seconds > 2419200) {
                event.with().ephemeral(true).reply("invalid-duration-limit");
                return;
            }
            moderationActBuilder.duration(duration.get());
        }
        onModerate(event, reason);
    }

    @Modal(value = "reason-title")
    public void onModerate(ModalEvent event, @TextInput("reason-field") String reason) {
        if (ModerationActService.isBanned(moderationActBuilder.targetId())) {
            event.with().embeds("userAlreadyBanned").reply();
            return;
        }
        executeModeration(event, reason);
    }
}
