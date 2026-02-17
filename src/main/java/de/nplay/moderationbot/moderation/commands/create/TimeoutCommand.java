package de.nplay.moderationbot.moderation.commands.create;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.duration.DurationMax;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.lock.Lock;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.rules.RuleService;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;


@Interaction
@Bundle("create")
@Permissions(BotPermissions.MODERATION_CREATE)
public class TimeoutCommand extends CreateCommand {

    private final ModerationActService actService;

    @Inject
    public TimeoutCommand(ModerationActService actService) {
        this.actService = actService;
    }

    @Lock("target")
    @Command("mod timeout")
    public void timeoutMember(
            CommandEvent event,
            Member target,
            @DurationMax(amount = 28, unit = ChronoUnit.DAYS) Duration until,
            @Param(optional = true, type = OptionType.INTEGER) RuleParagraph paragraph,
            @Param(optional = true) MessageLink messageLink
    ) {

        if (actService.isTimeOuted(target)) {
            event.reply(Replies.error("already-timeout"));
            return;
        }

        event.kv().put(BUILDER, ModerationActBuilder.timeout(target, event.getUser())
                .duration(until)
                .paragraph(paragraph)
                .messageReference(Helpers.retrieveMessage(event, messageLink)));

        replyModal(event, "Timeout");
    }
}
