package de.nplay.moderationbot.moderation.commands.modlog;

import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.components.container.SeparatedContainer;
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

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("modlog")
@Interaction
public class DetailCommand {

    @Command("mod detail")
    @Permissions(BotPermissions.MODERATION_READ)
    public void detail(CommandEvent event, @Param(type = OptionType.NUMBER) ModerationAct moderationAct) {
        SeparatedContainer container = SeparatedContainer.of(
                TextDisplay.of("detail"),
                Separator.createDivider(Spacing.SMALL)
        ).entries(
                entry("id", moderationAct.id()),
                entry("type", moderationAct.type().localized(event.getUserLocale())),
                entry("createdAt", moderationAct.createdAt()),
                entry("reason", moderationAct.reason()),
                entry("issuer", moderationAct.issuer())
        ).withAccentColor(Replies.STANDARD);

        moderationAct.paragraph().ifPresent(it ->
            container.add(TextDisplay.of("detail.paragraph"), entry("paragraph", it.fullDisplay()))
        );
        moderationAct.messageReference().ifPresent(it ->
             container.add(TextDisplay.of("detail.reference"), entry("reference", it.jumpUrl(event.getGuild())))
        );

        if (moderationAct instanceof RevertedModerationAct reverted) {
            container.add(
                    TextDisplay.of("detail.reverted"),
                    entry("reverter", reverted.revertedBy()),
                    entry("revertedAt", reverted.revertedAt()),
                    entry("revertingReason", reverted.revertingReason()),
                    entry("duration", Helpers.formatDuration(reverted.duration()))
            );
        } else if (moderationAct.revokeAt().isPresent()) {
            container.add(
                    TextDisplay.of("detail.revoke"),
                    entry("duration", Helpers.formatDuration(moderationAct.duration())),
                    entry("revokeAt", moderationAct.revokeAt().get())
            );
        }

        event.reply(container);
    }

}
