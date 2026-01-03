package de.nplay.moderationbot.moderation.commands.create;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.Serverlog;
import io.github.kaktushose.jdac.annotations.constraints.Max;
import io.github.kaktushose.jdac.annotations.constraints.Min;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Interaction
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
@Permissions(BotPermissions.MODERATION_CREATE)
public class BanCommands extends CreateCommands {

    @Inject
    public BanCommands(ModerationActLock moderationActLock, Serverlog serverlog) {
        super(moderationActLock, serverlog);
    }

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

        String title;
        if (until != null) {
            moderationActBuilder.duration(until);
            title = "Begründung angeben (Temp-Ban)";
        } else {
            title = "Begründung angeben (Ban)";
        }


    }

    @Modal(value = "reason-title")
    public void onModerate(ModalEvent event) {
        if (ModerationActService.isBanned(moderationActBuilder.targetId())) {
            event.with().embeds("userAlreadyBanned").reply();
            return;
        }
        executeModeration(event);
    }
}
