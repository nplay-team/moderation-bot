package de.nplay.moderationbot.serverlog;

import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.embeds.Embed;
import de.nplay.moderationbot.ModerationBot.EmbedColors;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.sql.Timestamp;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public class ModerationEvents {

    private static Embed createEmbed(ReplyableEvent<?> event, ModerationAct act) {
        Embed embed = event.embed("moderationCreateEvent").placeholders(
                entry("type", act.type().localizationKey()),
                entry("id", act.id()),
                entry("target", act.user()),
                entry("issuer", act.issuer()),
                entry("reason", act.reason()),
                entry("createdAt", act.createdAt()));
        act.revokeAt().ifPresent(it -> embed.fields().add("Aktiv bis", formatTimestamp(it.timestamp())));
        return embed;
    }

    private static Embed revertEmbed(ReplyableEvent<?> event, RevertedModerationAct act) {
        return genericModerationEmbed(event, "REVIDIERUNG", act)
                .placeholders(
                        entry("moderator", act.revertedBy()),
                        entry("color", EmbedColors.WARNING)
                );
    }

    private static Embed deleteEmbed(ReplyableEvent<?> event, RevertedModerationAct act) {
        Embed embed = genericModerationEmbed(event, "LÖSCHUNG", act)
                .placeholders(entry("color", EmbedColors.ERROR));
        embed.fields().remove("{ $revertedAt }");
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
                entry("target", target));
    }

    private static Embed genericModerationEmbed(ReplyableEvent<?> event, String action, RevertedModerationAct act) {
        return event.embed("moderationRemoveEvent").placeholders(
                entry("action", action),
                entry("type", event.resolve(act.type().localizationKey())),
                entry("id", act.id()),
                entry("target", act.user()),
                entry("issuer", act.issuer()),
                entry("revertedBy", act.revertedBy()),
                entry("revertedAt", act.revertedAt()),
                entry("revertingReason", act.revertingReason()));
    }

    private static Embed genericEmbed(ReplyableEvent<?> event, User issuer) {
        return event.embed("genericEvent").placeholders(
                entry("issuer", issuer),
                entry("createdAt", TimeFormat.DATE_TIME_SHORT.format(System.currentTimeMillis())));
    }

    private static String formatTimestamp(Timestamp timestamp) {
        return "%s (%s)".formatted(TimeFormat.DATE_TIME_LONG.format(timestamp.getTime()),
                                   TimeFormat.RELATIVE.atTimestamp(timestamp.getTime()));
    }
}
