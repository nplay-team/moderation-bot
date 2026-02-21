package de.nplay.moderationbot.moderation.commands.modlog;

import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.separator.Separator.Spacing;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.util.SeparatedContainer;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("modlog")
@Interaction
public class DetailCommand {

    @Command("mod detail")
    @Permissions(BotPermissions.MODERATION_READ)
    public void detail(CommandEvent event, @Param(type = OptionType.NUMBER) ModerationAct moderationAct) {
        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("detail"),
                Separator.createDivider(Spacing.SMALL),
                entry("id", moderationAct.id()),
                entry("type", moderationAct.type().localized(event.getUserLocale())),
                entry("createdAt", moderationAct.createdAt()),
                entry("reason", moderationAct.reason()),
                entry("issuer", moderationAct.issuer())
        ).withAccentColor(Replies.STANDARD);

        moderationAct.paragraph().ifPresent(it ->
            container.append(TextDisplay.of("detail.paragraph"), entry("paragraph", it.fullDisplay()))
        );
        moderationAct.referenceMessage().ifPresent(it ->
             container.append(TextDisplay.of("detail.reference"), entry("reference", it.jumpUrl(event.getGuild())))
        );

        if (moderationAct instanceof RevertedModerationAct reverted) {
            container.append(
                    TextDisplay.of("detail.reverted"),
                    entry("reverter", reverted.revertedBy()),
                    entry("revertedAt", reverted.revertedAt()),
                    entry("revertingReason", reverted.revertingReason())
            );
        } else if (moderationAct.revokeAt().isPresent()) {
            container.append(
                    TextDisplay.of("detail.revoke"),
                    entry("duration", Helpers.formatDuration(moderationAct.duration())),
                    entry("revokeAt", moderationAct.revokeAt().get())
            );
        }

        event.reply(container);
    }

}
