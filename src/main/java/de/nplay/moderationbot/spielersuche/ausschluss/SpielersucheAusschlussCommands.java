package de.nplay.moderationbot.spielersuche.ausschluss;

import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.google.inject.Inject;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.bot.BotConfig;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.moderation.ModerationUtils;
import de.nplay.moderationbot.moderation.create.ModerationActBuilder;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.serverlog.ModerationEvents;
import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import static de.nplay.moderationbot.Helpers.USER_HANDLER;


@Interaction
public class SpielersucheAusschlussCommands {

    @Inject
    private EmbedCache embedCache;

    @Inject
    private Serverlog serverlog;

    @Command(value = "spielersuche ausschluss", desc = "Schließt einen User von der Spielersuche aus und verwarnt ihn")
    @Permissions(BotPermissions.MODERATION_CREATE)
    public void spielersucheAusschluss(CommandEvent event, @Param("Der User, der ausgeschlossen werden soll") Member target,
                                       @Param(value = "Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?", optional = true) String paragraph) {
        var spielersucheAusschlussRolle = getSpielersucheAusschlussRolle(event);

        if (spielersucheAusschlussRolle.isEmpty()) {
            event.reply(embedCache.getEmbed("spielersucheRoleError").injectValue("color", EmbedColors.ERROR));
            return;
        }

        if (target.getRoles().contains(spielersucheAusschlussRolle.get())) {
            event.reply(EmbedHelpers.getEmbedWithTarget("spielersucheAlreadyBlocked", embedCache, target, EmbedColors.ERROR));
            return;
        }

        event.getGuild().addRoleToMember(target, spielersucheAusschlussRolle.get()).queue();

        var moderationActBuilder = ModerationActBuilder.warn(target, event.getUser()).reason("Du hast erneut gegen die Spielersucheregeln verstoßen **und wurdest von der Spielersuche ausgeschlossen!**");
        moderationActBuilder.paragraph(paragraph);

        var action = moderationActBuilder.build();
        var moderationAct = ModerationService.createModerationAct(action);
        action.executor().accept(action);
        ModerationUtils.sendMessageToTarget(moderationAct, event.getJDA(), target.getGuild(), embedCache);

        serverlog.onEvent(ModerationEvents.SpielersucheAusschluss(event.getJDA(), event.getGuild(), target.getUser(), event.getUser()));

        event.reply(EmbedHelpers.getEmbedWithTarget("spielersucheBlockSuccess", embedCache, target, EmbedColors.SUCCESS));
    }

    @Command(value = "spielersuche freigeben", desc = "Hebt den Ausschluss eines Users von der Spielersuche auf")
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void spielersucheFreigeben(CommandEvent event, @Param("Der User, dessen Ausschluss aufgehoben werden soll") Member target) {
        var spielersucheAusschlussRolle = getSpielersucheAusschlussRolle(event);

        if (spielersucheAusschlussRolle.isEmpty()) {
            event.reply(embedCache.getEmbed("spielersucheRoleError").injectValue("color", EmbedColors.ERROR));
            return;
        }

        if (!target.getRoles().contains(spielersucheAusschlussRolle.get())) {
            event.reply(EmbedHelpers.getEmbedWithTarget("spielersucheNotBlocked", embedCache, target, EmbedColors.ERROR));
            return;
        }

        event.getGuild().removeRoleFromMember(target, spielersucheAusschlussRolle.get()).queue();
        target.getUser()
                .openPrivateChannel()
                .flatMap(it -> it.sendMessageEmbeds(EmbedHelpers.getSpielersucheUnblockForTargetEmbed(embedCache, event.getUser()).toMessageEmbed()))
                .queue(_ -> {
                }, USER_HANDLER);

        serverlog.onEvent(ModerationEvents.SpielersucheAusschlussRevert(event.getJDA(), event.getGuild(), target.getUser(), event.getUser()));

        event.reply(EmbedHelpers.getEmbedWithTarget("spielersucheUnblockSuccess", embedCache, target, EmbedColors.SUCCESS));
    }

    private java.util.Optional<Role> getSpielersucheAusschlussRolle(CommandEvent event) {
        var spielersucheAusschlussRolleId = ConfigService.get(BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE);
        return spielersucheAusschlussRolleId.map(s -> event.getGuild().getRoleById(s));
    }

}
