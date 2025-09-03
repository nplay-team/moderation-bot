package de.nplay.moderationbot.moderation.create;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command.Type;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class WarnCommands extends CreateCommands {

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

    @Command(value = "Verwarne Mitglied", type = Type.USER)
    public void warnMemberContext(CommandEvent event, User target) {
        if (moderationActLock.checkLocked(event, target, event.getUser())) {
            return;
        }
        moderationActBuilder = ModerationActBuilder.warn(event.getGuild().retrieveMember(target).complete(), event.getUser());
        replyEphemeral = true;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Warn)"));
    }

    @Command(value = "Verwarne Mitglied (💬)", type = Type.MESSAGE)
    public void warnMemberMessageContext(CommandEvent event, Message target) {
        if (moderationActLock.checkLocked(event, target.getAuthor(), event.getUser())) {
            return;
        }
        moderationActBuilder = ModerationActBuilder.warn(target.getMember(), event.getUser()).messageReference(target);
        replyEphemeral = true;
        event.replyModal("onModerate", modal -> modal.title("Begründung angeben (Warn)"));
    }

    @Modal(value = "reason-title")
    public void onModerate(ModalEvent event, @TextInput("reason-field") String reason) {
        executeModeration(event, reason);
    }
}
