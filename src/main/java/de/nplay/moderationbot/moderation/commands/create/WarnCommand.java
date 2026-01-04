package de.nplay.moderationbot.moderation.commands.create;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.Member;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class WarnCommand extends CreateCommand {

    @Inject
    public WarnCommand(ModerationActLock moderationActLock) {
        super(moderationActLock);
    }

    @Command("mod warn")
    public void warnMember(CommandEvent event, Member target,
                           @Param(optional = true) String paragraph, @Param(optional = true) MessageLink messageLink) {
        if (checkLocked(event, target, event.getUser())) {
            return;
        }
        event.kv().put(BUILDER, ModerationActBuilder.warn(target, event.getUser())
                .paragraph(paragraph)
                .messageReference(Helpers.retrieveMessage(event, messageLink)));

        replyModal(event, "Warn");
    }
}
