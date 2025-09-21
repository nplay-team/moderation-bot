package de.nplay.moderationbot.serverlog;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.embeds.Embed;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.NPLAYModerationBot.EmbedColors;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

public class ModerationEvents {

    public static BotEvent Created(JDA jda, Guild guild, ModerationAct moderationAct) {
        return new BotEvent(jda, guild, (event) -> createEmbed(event, moderationAct));
    }

    public static BotEvent Reverted(JDA jda, Guild guild, RevertedModerationAct moderationAct) {
        return new BotEvent(jda, guild, (event) -> revertEmbed(event, moderationAct));
    }

    public static BotEvent Deleted(JDA jda, Guild guild, RevertedModerationAct moderationAct) {
        return new BotEvent(jda, guild, (event) -> deleteEmbed(event, moderationAct));
    }

    public static BotEvent BulkMessageDeletion(JDA jda, Guild guild, Integer amount, User user) {
        return new BotEvent(jda, guild, (event) -> bulkMessageDeletionEmbed(event, amount, user));
    }

    public static BotEvent SpielersucheAusschluss(JDA jda, Guild guild, User target, User issuer) {
        return new BotEvent(jda, guild, (event) ->
                spielersucheAusschlussEmbed(event, target, issuer, false)
        );
    }

    public static BotEvent SpielersucheAusschlussRevert(JDA jda, Guild guild, User target, User issuer) {
        return new BotEvent(jda, guild, (event) ->
                spielersucheAusschlussEmbed(event, target, issuer, true)
        );
    }

    private static Embed createEmbed(ReplyableEvent<?> event, ModerationAct act) {
        Embed embed = event.embed("moderationCreateEvent").placeholders(
                entry("type", act.type()),
                entry("id", act.id()),
                entry("target", Helpers.formatUser(event.getJDA(), act.user())),
                entry("issuer", Helpers.formatUser(event.getJDA(), act.issuer())),
                entry("reason", act.reason()),
                entry("createdAt", TimeFormat.DATE_TIME_LONG.format(act.createdAt().getTime())));
        act.revokeAt().ifPresent(it -> embed.fields().add("Aktiv bis", Helpers.formatTimestamp(it)));
        return embed;
    }

    private static Embed revertEmbed(ReplyableEvent<?> event, RevertedModerationAct act) {
        return genericModerationEmbed(event, "REVIDIERUNG", act)
                .placeholders(
                        entry("moderator", Helpers.formatUser(event.getJDA(), act.revertedBy())),
                        entry("color", EmbedColors.WARNING)
                );
    }

    private static Embed deleteEmbed(ReplyableEvent<?> event, RevertedModerationAct act) {
        Embed embed = genericModerationEmbed(event, "LÖSCHUNG", act)
                .placeholders(entry("color", EmbedColors.ERROR));
        embed.fields().removeByName("Datum");
        return embed;
    }

    private static Embed bulkMessageDeletionEmbed(ReplyableEvent<?> event, Integer amount, User issuer) {
        Embed embed = genericEmbed(event, issuer).placeholders(
                entry("title", "Massenlöschung von Nachrichten")
        );
        embed.fields().replace("Betroffener", new MessageEmbed.Field("Menge", "%d Nachrichten".formatted(amount), false));
        return embed;
    }

    private static Embed spielersucheAusschlussEmbed(ReplyableEvent<?> event, User target, User issuer, Boolean reverted) {
        return genericEmbed(event, issuer).placeholders(
                entry("title", "Spielersuche-Ausschluss" + (reverted ? "-Aufhebung" : "")),
                entry("target", Helpers.formatUser(event.getJDA(), target)));
    }

    private static Embed genericModerationEmbed(ReplyableEvent<?> event, String action, RevertedModerationAct act) {
        return event.embed("moderationRemoveEvent").placeholders(
                entry("action", action),
                entry("type", act.type().toString()),
                entry("id", act.id()),
                entry("target", Helpers.formatUser(event.getJDA(), act.user())),
                entry("issuer", Helpers.formatUser(event.getJDA(), act.issuer())),
                entry("revertedBy", Helpers.formatUser(event.getJDA(), act.revertedBy())),
                entry("revertedAt", TimeFormat.DATE_TIME_LONG.format(act.revertedAt().getTime())),
                entry("revertingReason", act.revertingReason()));
    }

    private static Embed genericEmbed(ReplyableEvent<?> event, User issuer) {
        return event.embed("genericEvent").placeholders(
                entry("issuer", Helpers.formatUser(event.getJDA(), issuer)),
                entry("createdAt", TimeFormat.DATE_TIME_SHORT.format(System.currentTimeMillis())));
    }
}
