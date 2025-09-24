package de.nplay.moderationbot.spielersuche;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.Embed;
import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.moderation.act.model.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.util.Optional;

import static com.github.kaktushose.jda.commands.message.i18n.I18n.entry;


@Interaction("spielersuche")
public class SpielersucheAusschlussCommands {

    @Inject
    private Serverlog serverlog;

    @Command("ausschluss")
    @Permissions(BotPermissions.MODERATION_CREATE)
    public void spielersucheAusschluss(CommandEvent event, Member target, @Param(optional = true) String paragraph) {
        var spielersucheAusschlussRolle = getSpielersucheAusschlussRolle(event);

        if (spielersucheAusschlussRolle.isEmpty()) {
            event.with().embeds("spielersucheRoleError").reply();
            return;
        }

        if (target.getRoles().contains(spielersucheAusschlussRolle.get())) {
            event.with().embeds(embed(event, "spielersucheAlreadyBlocked", target)).reply();
            return;
        }
        event.getGuild().addRoleToMember(target, spielersucheAusschlussRolle.get()).queue();

        ModerationActBuilder.warn(target, event.getUser())
                .reason(event.i18n().localize(event.getUserLocale().toLocale(), "spielersuche-ausschluss-reason"))
                .paragraph(paragraph)
                .execute(event);

        serverlog.onEvent(ModerationEvents.SpielersucheAusschluss(event.getJDA(), event.getGuild(), target.getUser(), event.getUser()), event);

        event.with().embeds(embed(event, "spielersucheBlockSuccess", target)).reply();
    }

    @Command(value = "freigeben")
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void spielersucheFreigeben(CommandEvent event, Member target) {
        var spielersucheAusschlussRolle = getSpielersucheAusschlussRolle(event);

        if (spielersucheAusschlussRolle.isEmpty()) {
            event.with().embeds("spielersucheRoleError").reply();
            return;
        }

        if (!target.getRoles().contains(spielersucheAusschlussRolle.get())) {
            event.with().embeds(embed(event, "spielersucheNotBlocked", target)).reply();
            return;
        }
        event.getGuild().removeRoleFromMember(target, spielersucheAusschlussRolle.get()).queue();

        Helpers.sendDM(target, event.getJDA(), channel ->
                channel.sendMessageEmbeds(event.embed("spielersucheUnblockForTarget").placeholders(
                        entry("issuer", Helpers.formatUser(event.getJDA(), event.getUser())),
                        entry("createdAt", TimeFormat.DATE_TIME_LONG.format(System.currentTimeMillis()))).build())
        );

        serverlog.onEvent(ModerationEvents.SpielersucheAusschlussRevert(event.getJDA(), event.getGuild(), target.getUser(), event.getUser()), event);

        event.with().embeds(embed(event, "spielersucheUnblockSuccess", target)).reply();
    }

    private Optional<Role> getSpielersucheAusschlussRolle(CommandEvent event) {
        var spielersucheAusschlussRolleId = ConfigService.get(BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE);
        return spielersucheAusschlussRolleId.map(s -> event.getGuild().getRoleById(s));
    }

    private Embed embed(ReplyableEvent<?> event, String embedName, Member target) {
        return event.embed(embedName).placeholders(entry("target", Helpers.formatUser(event.getJDA(), target)));
    }
}
