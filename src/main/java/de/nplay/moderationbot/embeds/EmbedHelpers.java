package de.nplay.moderationbot.embeds;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.bot.BotConfig;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.notes.NotesService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;

import static de.nplay.moderationbot.moderation.modlog.ModlogCommand.ModlogContext;

public class EmbedHelpers {

    public static EmbedDTO getEmbedWithTarget(String embedName, EmbedCache embedCache, Member target, EmbedColors color) {
        return embedCache.getEmbed(embedName)
                .injectValue("targetId", target.getId())
                .injectValue("targetUsername", target.getUser().getName())
                .injectValue("color", color);
    }

    public static MessageEmbed getModlogEmbedHeader(EmbedCache embedCache, ModlogContext context) {
        var spielersucheAusschlussRolle = ConfigService.get(BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE);

        var embed = embedCache.getEmbed("modlogHeader")
                .injectValue("username", context.user().getEffectiveName())
                .injectValue("userId", context.user().getIdLong())
                .injectValue("avatarUrl", context.user().getEffectiveAvatarUrl())
                .injectValue("createdAt", context.user().getTimeCreated().getLong(ChronoField.INSTANT_SECONDS))
                .injectValue("color", EmbedColors.DEFAULT);

        if (context.member() == null) {
            embed.injectValue("roles", "?DEL?")
                    .injectValue("joinedAt", "?DEL?");
        } else {
            var roles = context.member().getRoles().stream()
                    .filter(it -> it.getId().equals(spielersucheAusschlussRolle.orElse("-1")))
                    .map(it -> "<@&%s>".formatted(it.getId()))
                    .reduce((a, b) -> a + " " + b)
                    .orElse("?DEL?");

            embed.injectValue("roles", roles)
                    .injectValue("joinedAt", context.member().getTimeJoined().getLong(ChronoField.INSTANT_SECONDS));

        }
        var builder = embed.toEmbedBuilder();

        builder.getFields().removeIf(it -> Objects.requireNonNullElse(it.getValue(), "").contains("?DEL?"));
        return builder.build();
    }

    public static EmbedDTO getModlogEmbed(EmbedCache embedCache, JDA jda, List<ModerationService.ModerationAct> moderationActs, Integer page, Integer maxPage) {
        var embed = embedCache.getEmbed("modlogActs")
                .injectValue("page", page)
                .injectValue("maxPage", maxPage)
                .injectValue("color", EmbedColors.DEFAULT);

        embed.setFields(moderationActs.stream().map(it -> it.getEmbedField(jda)).toArray(EmbedDTO.Field[]::new));
        return embed;
    }

    public static EmbedDTO getNotesCreatedEmbed(EmbedCache embedCache, JDA jda, NotesService.Note note) {
        var targetUsername = jda.retrieveUserById(note.userId()).complete().getName();
        var creatorUsername = jda.retrieveUserById(note.creatorId()).complete().getName();

        return embedCache.getEmbed("noteCreated")
                .injectValue("id", note.id())
                .injectValue("content", note.content())
                .injectValue("targetId", note.userId())
                .injectValue("targetUsername", targetUsername)
                .injectValue("createdById", note.creatorId())
                .injectValue("createdByUsername", creatorUsername)
                .injectValue("createdAt", note.createdAt().getTime() / 1000)
                .injectValue("color", EmbedColors.SUCCESS);
    }

    public static EmbedDTO getNotesEmbed(EmbedCache embedCache, JDA jda, UserSnowflake target, List<NotesService.Note> notes) {
        var targetUsername = jda.retrieveUserById(target.getIdLong()).complete().getName();
        var embed = embedCache.getEmbed("noteList").injectValue("target", targetUsername).injectValue("color", EmbedColors.DEFAULT);
        embed.setFields(notes.stream().map(it -> it.getEmbedField(jda)).toArray(EmbedDTO.Field[]::new));
        return embed;
    }

    public static EmbedDTO getBulkMessageDeletionSuccessfulEmbed(EmbedCache embedCache, int amount) {
        return embedCache.getEmbed("bulkDeleteSuccessful")
                .injectValue("amount", amount)
                .injectValue("color", EmbedColors.SUCCESS);
    }

    public static EmbedDTO getSpielersucheUnblockForTargetEmbed(EmbedCache embedCache, User issuer) {
        return embedCache.getEmbed("spielersucheUnblockForTarget")
                .injectValue("issuerId", issuer.getId())
                .injectValue("issuerUsername", issuer.getName())
                .injectValue("createdAt", System.currentTimeMillis() / 1000)
                .injectValue("color", EmbedColors.DEFAULT);
    }

    // EVENT EMBEDS //

    public static EmbedDTO getGenericModerationEventEmbed(EmbedCache embedCache, String name, JDA jda, ModerationService.ModerationAct moderationAct,
                                                          @Nullable User deleter) {
        var targetUsername = jda.retrieveUserById(moderationAct.userId()).complete().getName();
        var issuerUsername = jda.retrieveUserById(moderationAct.issuerId()).complete().getName();
        var revertedUsername = moderationAct.revertedBy() != null ? jda.retrieveUserById(moderationAct.revertedBy()).complete().getName() : null;
        return embedCache.getEmbed(name)
                .injectValue("type", moderationAct.type().humanReadableString)
                .injectValue("id", moderationAct.id())
                .injectValue("targetId", moderationAct.userId())
                .injectValue("targetUsername", targetUsername)
                .injectValue("issuerId", moderationAct.issuerId())
                .injectValue("issuerUsername", issuerUsername)
                .injectValue("revertedById", moderationAct.revertedBy())
                .injectValue("revertedByUsername", revertedUsername)
                .injectValue("deletedById", deleter != null ? deleter.getId() : null)
                .injectValue("deletedByUsername", deleter != null ? deleter.getName() : null)
                .injectValue("revertedAt", moderationAct.revertedAt() != null ? moderationAct.revertedAt().getTime() / 1000 : null)
                .injectValue("revertingReason", Objects.requireNonNullElse(moderationAct.revertingReason(), "Kein Grund angegeben"))
                .injectValue("reason", Objects.requireNonNullElse(moderationAct.reason(), "Kein Grund angegeben"))
                .injectValue("createdAt", moderationAct.createdAt().getTime() / 1000)
                .injectValue("until", moderationAct.duration() == null ? "Keine temporäre Handlung" : "<t:%d:F>".formatted((moderationAct.createdAt().getTime() + moderationAct.duration()) / 1000))
                .injectValue("color", EmbedColors.DEFAULT)
                .injectValue("warningColor", EmbedColors.WARNING)
                .injectValue("deleteColor", EmbedColors.ERROR);
    }

    public static EmbedDTO getBulkMessageDeletionEmbed(EmbedCache embedCache, @NotNull Integer amount, @NotNull User user) {
        return embedCache.getEmbed("bulkMessageDeleteEvent")
                .injectValue("amount", amount)
                .injectValue("issuerId", user.getId())
                .injectValue("issuerUsername", user.getName())
                .injectValue("createdAt", System.currentTimeMillis() / 1000)
                .injectValue("color", EmbedColors.DEFAULT);
    }

    public static EmbedDTO getSpielersucheAusschlussEmbed(EmbedCache embedCache, @NotNull User target, @NotNull User issuer, Boolean reverted) {
        return embedCache.getEmbed("spielersucheAusschluss" + (reverted ? "Revert" : "") + "Event")
                .injectValue("targetId", target.getId())
                .injectValue("targetUsername", target.getName())
                .injectValue("issuerId", issuer.getId())
                .injectValue("issuerUsername", issuer.getName())
                .injectValue("createdAt", System.currentTimeMillis() / 1000)
                .injectValue("color", EmbedColors.DEFAULT);
    }
}
