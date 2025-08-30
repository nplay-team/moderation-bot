package de.nplay.moderationbot.embeds;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.embeds.Embed;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.bot.BotConfig;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.notes.NotesService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.annotation.Nullable;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;
import static de.nplay.moderationbot.moderation.modlog.ModlogCommand.ModlogContext;

public class EmbedHelpers {

    public static Embed getEmbedWithTarget(String embedName, ReplyableEvent<?> event, Member target) {
        return event.embed(embedName).placeholders(
                entry("targetId", target.getId()),
                entry("targetUsername", target.getUser().getName()));
    }

    public static Embed getModlogEmbedHeader(ReplyableEvent<?> event, ModlogContext context) {
        var spielersucheAusschlussRolle = ConfigService.get(BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE);

        var embed = event.embed("modlogHeader").placeholders(
                entry("username", context.user().getName()),
                entry("effectiveName", context.user().getEffectiveName()),
                entry("userId", context.user().getIdLong()),
                entry("avatarUrl", context.user().getEffectiveAvatarUrl()),
                entry("createdAt", context.user().getTimeCreated().getLong(ChronoField.INSTANT_SECONDS)));

        if (context.member() == null) {
            embed.placeholders(entry("roles", "?DEL?"),
                    entry("joinedAt", "?DEL?"));
        } else {
            var roles = context.member().getRoles().stream()
                    .filter(it -> it.getId().equals(spielersucheAusschlussRolle.orElse("-1")))
                    .map(it -> "<@&%s>".formatted(it.getId()))
                    .reduce((a, b) -> a + " " + b)
                    .orElse("?DEL?");

            embed.placeholders(entry("roles", roles),
                    entry("joinedAt", context.member().getTimeJoined().getLong(ChronoField.INSTANT_SECONDS)));

        }
        embed.fields().remove("?DEL?");
        return embed;
    }

    public static Embed getModlogEmbed(ReplyableEvent<?> event, JDA jda, List<ModerationService.ModerationAct> moderationActs, Integer page, Integer maxPage) {
        var embed = event.embed("modlogActs").placeholders(
                entry("page", page),
                entry("maxPage", maxPage));

        embed.getFields().addAll(moderationActs.stream().map(it -> it.getEmbedField(jda)).toList());
        return embed;
    }

    public static Embed getNotesCreatedEmbed(ReplyableEvent<?> event, JDA jda, NotesService.Note note) {
        var targetUsername = jda.retrieveUserById(note.userId()).complete().getName();
        var creatorUsername = jda.retrieveUserById(note.creatorId()).complete().getName();

        return event.embed("noteCreated").placeholders(
                entry("id", note.id()),
                entry("content", note.content()),
                entry("targetId", note.userId()),
                entry("targetUsername", targetUsername),
                entry("createdById", note.creatorId()),
                entry("createdByUsername", creatorUsername),
                entry("createdAt", note.createdAt().getTime() / 1000));
    }

    public static Embed getNotesEmbed(ReplyableEvent<?> event, JDA jda, UserSnowflake target, List<NotesService.Note> notes) {
        var targetUsername = jda.retrieveUserById(target.getIdLong()).complete().getName();
        var embed = event.embed("noteList").placeholders(entry("target", targetUsername));
        embed.getFields().addAll(notes.stream().map(it -> it.getEmbedField(jda)).toList());
        return embed;
    }

    public static Embed getSpielersucheUnblockForTargetEmbed(ReplyableEvent<?> event, User issuer) {
        return event.embed("spielersucheUnblockForTarget").placeholders(
                entry("issuerId", issuer.getId()),
                entry("issuerUsername", issuer.getName()),
                entry("createdAt", System.currentTimeMillis() / 1000));
    }

    // EVENT EMBEDS //

    public static Embed getGenericModerationEventEmbed(ReplyableEvent<?> event, String name, JDA jda, ModerationService.ModerationAct moderationAct,
                                                       @Nullable User deleter) {
        var targetUsername = jda.retrieveUserById(moderationAct.userId()).complete().getName();
        var issuerUsername = jda.retrieveUserById(moderationAct.issuerId()).complete().getName();
        var revertedUsername = moderationAct.revertedBy() != null ? jda.retrieveUserById(moderationAct.revertedBy()).complete().getName() : null;
        var embed = event.embed(name).placeholders(
                entry("type", moderationAct.type().humanReadableString),
                entry("id", moderationAct.id()),
                entry("targetId", moderationAct.userId()),
                entry("targetUsername", targetUsername),
                entry("issuerId", moderationAct.issuerId()),
                entry("issuerUsername", issuerUsername),
                entry("revertedById", moderationAct.revertedBy()),
                entry("revertedByUsername", revertedUsername),
                entry("deletedById", deleter != null ? deleter.getId() : null),
                entry("deletedByUsername", deleter != null ? deleter.getName() : null),
                entry("revertedAt", moderationAct.revertedAt() != null ? moderationAct.revertedAt().getTime() / 1000 : null),
                entry("revertingReason", Objects.requireNonNullElse(moderationAct.revertingReason(), "Kein Grund angegeben")),
                entry("reason", Objects.requireNonNullElse(moderationAct.reason(), "Kein Grund angegeben")),
                entry("createdAt", moderationAct.createdAt().getTime() / 1000),
                entry("until", moderationAct.duration() == null ? "?DEL?" : "<t:%d:F>".formatted((moderationAct.createdAt().getTime() + moderationAct.duration()) / 1000)),
                entry("color", EmbedColors.DEFAULT),
                entry("warningColor", EmbedColors.WARNING),
                entry("deleteColor", EmbedColors.ERROR));

        embed.fields().remove("?DEL?");
        return embed;
    }

    public static Embed getBulkMessageDeletionEmbed(ReplyableEvent<?> event, Integer amount, User user) {
        return event.embed("bulkMessageDeleteEvent").placeholders(
                entry("amount", amount),
                entry("issuerId", user.getId()),
                entry("issuerUsername", user.getName()),
                entry("createdAt", System.currentTimeMillis() / 1000));
    }

    public static Embed getSpielersucheAusschlussEmbed(ReplyableEvent<?> event, User target, User issuer, Boolean reverted) {
        return event.embed("spielersucheAusschluss" + (reverted ? "Revert" : "") + "Event").placeholders(
                entry("targetId", target.getId()),
                entry("targetUsername", target.getName()),
                entry("issuerId", issuer.getId()),
                entry("issuerUsername", issuer.getName()),
                entry("createdAt", System.currentTimeMillis() / 1000));
    }
}
