package de.nplay.moderationbot.moderation.commands.create;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.lock.Lock;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.constraints.Max;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@Interaction
@Bundle("create")
@Permissions(BotPermissions.MODERATION_CREATE)
@CommandConfig(enabledFor = Permission.KICK_MEMBERS)
public class KickCommand extends CreateCommand {

    @Lock("target")
    @Command("mod kick")
    public void kickMember(CommandEvent event,
                           Member target,
                           @Param(optional = true) String paragraph,
                           @Param(optional = true) @Max(7) int delDays,
                           @Param(optional = true) MessageLink messageLink) {
        event.kv().put(BUILDER, ModerationActBuilder.kick(target, event.getUser()).paragraph(paragraph).
                deletionDays(delDays)
                .messageReference(Helpers.retrieveMessage(event, messageLink)));

        replyModal(event, "Kick");
    }
}
