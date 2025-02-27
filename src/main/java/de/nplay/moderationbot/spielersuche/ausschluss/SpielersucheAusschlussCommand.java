package de.nplay.moderationbot.spielersuche.ausschluss;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.AutoCompleteEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.config.bot.BotConfigs;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.moderation.ModerationUtils;
import de.nplay.moderationbot.moderation.create.ModerationActBuilder;
import de.nplay.moderationbot.moderation.create.ModerationCommands;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import static de.nplay.moderationbot.Helpers.UNKNOWN_USER_HANDLER;


@Interaction
public class SpielersucheAusschlussCommand {

    @Inject
    private EmbedCache embedCache;

    @AutoComplete("spielersuche ausschluss")
    public void onParagraphAutocomplete(AutoCompleteEvent event) {
        new ModerationCommands().onParagraphAutocomplete(event);
    }

    @SlashCommand(value = "spielersuche ausschluss", desc = "Schließt einen User von der Spielersuche aus und verwarnt ihn", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    @Permissions(BotPermissions.MODERATION_CREATE)
    public void spielersucheAusschluss(CommandEvent event, @Param("Der User, der ausgeschlossen werden soll") Member target,
                                       @Optional @Param("Welcher Regel-Paragraph ist verletzt worden / soll referenziert werden?") String paragraph) {
        var spielersucheAusschlussRolle = getSpielersucheAusschlussRolle(event);

        if (spielersucheAusschlussRolle == null) {
            event.reply(embedCache.getEmbed("spielersucheRoleError").injectValue("color", EmbedColors.ERROR));
            return;
        }

        if (target.getRoles().contains(spielersucheAusschlussRolle)) {
            event.reply(EmbedHelpers.getEmbedWithTarget("spielersucheAlreadyBlocked", embedCache, target, EmbedColors.ERROR));
            return;
        }

        event.getGuild().addRoleToMember(target, spielersucheAusschlussRolle).queue();

        var moderationActBuilder = ModerationActBuilder.warn(target, event.getUser()).reason("Du hast erneut gegen die Spielersucheregeln verstoßen **und wurdest von der Spielersuche ausgeschlossen!**");
        if (paragraph != null) moderationActBuilder.paragraph(paragraph);

        var moderationAct = ModerationService.createModerationAct(moderationActBuilder.build());
        ModerationUtils.sendMessageToTarget(moderationAct, event.getJDA(), target.getGuild(), embedCache);

        event.reply(EmbedHelpers.getEmbedWithTarget("spielersucheBlockSuccess", embedCache, target, EmbedColors.SUCCESS));
    }

    @SlashCommand(value = "spielersuche freigeben", desc = "Hebt den Ausschluss eines Users von der Spielersuche auf", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    @Permissions(BotPermissions.MODERATION_REVERT)
    public void spielersucheFreigeben(CommandEvent event, @Param("Der User, dessen Ausschluss aufgehoben werden soll") Member target) {
        var spielersucheAusschlussRolle = getSpielersucheAusschlussRolle(event);

        if (spielersucheAusschlussRolle == null) {
            event.reply(embedCache.getEmbed("spielersucheRoleError").injectValue("color", EmbedColors.ERROR));
            return;
        }

        if (!target.getRoles().contains(spielersucheAusschlussRolle)) {
            event.reply(EmbedHelpers.getEmbedWithTarget("spielersucheNotBlocked", embedCache, target, EmbedColors.ERROR));
            return;
        }

        event.getGuild().removeRoleFromMember(target, spielersucheAusschlussRolle).queue();
        target.getUser()
                .openPrivateChannel()
                .flatMap(it -> it.sendMessageEmbeds(EmbedHelpers.getSpielersucheUnblockForTargetEmbed(embedCache, event.getUser()).toMessageEmbed()))
                .queue(_ -> {
                }, UNKNOWN_USER_HANDLER);

        event.reply(EmbedHelpers.getEmbedWithTarget("spielersucheUnblockSuccess", embedCache, target, EmbedColors.SUCCESS));
    }

    private Role getSpielersucheAusschlussRolle(CommandEvent event) {
        var spielersucheAusschlussRolleId = BotConfigs.SpielersucheAusschlussRolle(event.getJDA()).value();
        return spielersucheAusschlussRolleId.map(s -> event.getGuild().getRoleById(s)).orElse(null);
    }

}
