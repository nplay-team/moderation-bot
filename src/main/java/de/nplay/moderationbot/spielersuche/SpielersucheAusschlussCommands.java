package de.nplay.moderationbot.spielersuche;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.auditlog.lifecycle.Lifecycle;
import de.nplay.moderationbot.auditlog.lifecycle.events.SpielersucheAusschlussEvent;
import de.nplay.moderationbot.auditlog.lifecycle.events.SpielersucheFreigabeEvent;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
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
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("spielersuche")
@Interaction("spielersuche")
public class SpielersucheAusschlussCommands {

    private final ModerationActService actService;
    private final ConfigService configService;
    private final Lifecycle lifecycle;

    @Inject
    public SpielersucheAusschlussCommands(ModerationActService actService, ConfigService configService, Lifecycle lifecycle) {
        this.actService = actService;
        this.configService = configService;
        this.lifecycle = lifecycle;
    }

    @Command("ausschluss")
    @Permissions(BotPermissions.MODERATION_CREATE)
    public void spielersucheAusschluss(CommandEvent event, Member target, @Param(optional = true, type = OptionType.INTEGER) RuleParagraph paragraph) {
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
                .execute(event, actService);

        lifecycle.publish(new SpielersucheAusschlussEvent(target, event.getUser()));
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
        Helpers.sendDM(target, event.getJDA(),container);

        lifecycle.publish(new SpielersucheFreigabeEvent(target, event.getUser()));
        event.reply(Replies.success("unblock"), entry("target", target));
    }

    private Optional<Role> role(CommandEvent event) {
        var role = configService.get(BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE);
        return role.map(it -> event.getGuild().getRoleById(it));
    }
}
