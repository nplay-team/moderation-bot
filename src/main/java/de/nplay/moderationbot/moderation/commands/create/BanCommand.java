package de.nplay.moderationbot.moderation.commands.create;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.lock.Lock;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.constraints.Max;
import io.github.kaktushose.jdac.annotations.constraints.Min;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

@Interaction
@Bundle("create")
@Permissions(BotPermissions.MODERATION_CREATE)
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
public class BanCommand extends CreateCommand {

    @Lock("target")
    @Command("mod ban")
    public void banMember(
            CommandEvent event,
            User target,
            @Param(optional = true) @Nullable Duration until,
            @Param(optional = true) @Min(1) @Max(7) int delDays,
            @Param(optional = true) @Nullable String paragraph,
            @Param(optional = true) @Nullable MessageLink messageLink
    ) {
        if (ModerationActService.isBanned(target.getIdLong())) {
            event.reply(Replies.error("user-already-banned"));
            return;
        }

        Member member;
        ModerationActBuilder builder;
        try {
            member = event.getGuild().retrieveMember(target).complete();
            builder = ModerationActBuilder.ban(member, event.getUser()).deletionDays(delDays);
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER) {
                builder = ModerationActBuilder.ban(target, event.getGuild(), event.getUser()).deletionDays(delDays);
            } else {
                throw new IllegalStateException(e);
            }
        }

        builder.paragraph(paragraph).messageReference(Helpers.retrieveMessage(event, messageLink));

        String type;
        if (until != null) {
            builder.duration(until);
            type = "Temp-Bann";
        } else {
            type = "Bann";
        }

        event.kv().put(BUILDER, builder);
        replyModal(event, type);
    }
}
