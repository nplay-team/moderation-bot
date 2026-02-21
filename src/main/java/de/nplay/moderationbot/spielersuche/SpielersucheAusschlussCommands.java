package de.nplay.moderationbot.spielersuche;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Param;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("spielersuche")
@Interaction("spielersuche")
public class SpielersucheAusschlussCommands {

    private final Serverlog serverlog;

    @Inject
    public SpielersucheAusschlussCommands(Serverlog serverlog) {
        this.serverlog = serverlog;
    }

    @Command("ausschluss")
    @Permissions(BotPermissions.MODERATION_CREATE)
    public void spielersucheAusschluss(CommandEvent event, Member target, @Param(optional = true) String paragraph) {
        var role = role(event);
        if (role.isEmpty()) {
            event.reply(Replies.error("role-error"));
            return;
        }
        if (target.getRoles().contains(role.get())) {
            event.reply(Replies.error("already-blocked"), entry("target", target));
            return;
        }

        event.getGuild().addRoleToMember(target, role.get()).queue();
        ModerationActBuilder.warn(target, event.getUser())
                .reason(event.resolve("spielersuche-ausschluss-reason"))
                .paragraph(paragraph)
                .execute(event);

        serverlog.onEvent(ModerationEvents.SpielersucheAusschluss(event.getJDA(), event.getGuild(), target.getUser(), event.getUser()), event);
        event.reply(Replies.success("block"), entry("target", target));
    }

    @Command(value = "freigeben")
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void spielersucheFreigeben(CommandEvent event, Member target) {
        var role = role(event);
        if (role.isEmpty()) {
            event.reply(Replies.error("role-error"));
            return;
        }
        if (!target.getRoles().contains(role.get())) {
            event.reply(Replies.error("not-blocked"), entry("target", target));
            return;
        }

        event.getGuild().removeRoleFromMember(target, role.get()).queue();
        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("unblock-target"),
                Separator.createDivider(Separator.Spacing.SMALL),
                entry("issuer", event.getUser()),
                entry("createdAt", AbsoluteTime.now())
        ).append(TextDisplay.of("unblock-target.body")).withAccentColor(Replies.STANDARD);
        Helpers.sendDM(target, event.getJDA(), channel -> channel.sendMessageComponents(container).useComponentsV2());

        serverlog.onEvent(ModerationEvents.SpielersucheAusschlussRevert(event.getJDA(), event.getGuild(), target.getUser(), event.getUser()), event);
        event.reply(Replies.success("unblock"), entry("target", target));
    }

    private Optional<Role> role(CommandEvent event) {
        var role = ConfigService.get(BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE);
        return role.map(it -> event.getGuild().getRoleById(it));
    }
}
