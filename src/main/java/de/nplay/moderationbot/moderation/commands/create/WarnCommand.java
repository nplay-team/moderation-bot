package de.nplay.moderationbot.moderation.commands.create;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.moderation.lock.Lock;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static de.nplay.moderationbot.moderation.commands.create.CreateCommandHelpers.BUILDER;
import static de.nplay.moderationbot.moderation.commands.create.CreateCommandHelpers.replyModal;

@Interaction
@Bundle("create")
@Permissions(BotPermissions.MODERATION_CREATE)
public class WarnCommand {
	
	@Lock("target")
	@Command("mod warn")
	public void warnMember(
			CommandEvent event,
			Member target,
			@Param(optional = true, type = OptionType.INTEGER) RuleParagraph paragraph,
			@Param(optional = true) MessageLink messageLink
	) {
		event.keyValueStore().put(BUILDER, ModerationActBuilder.warn(target, event.getUser())
				.paragraph(paragraph)
				.messageReference(Helpers.retrieveMessage(event, messageLink)));
		
		replyModal(event, "Warn");
	}
}
