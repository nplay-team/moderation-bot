package de.nplay.moderationbot.embeds;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.moderation.ModerationService;
import de.nplay.moderationbot.notes.NotesService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.annotation.Nullable;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;

public class EmbedHelpers {

    public static EmbedDTO getModlogEmbedHeader(EmbedCache embedCache, Member member) {
        return embedCache.getEmbed("modlogHeader")
                .injectValue("username", member.getUser().getEffectiveName())
                .injectValue("userId", member.getIdLong())
                .injectValue("avatarUrl", member.getUser().getEffectiveAvatarUrl())
                .injectValue("roles", member.getRoles().stream().map(Role::getName).reduce((a, b) -> a + ", " + b).orElse("Keine Rollen"))
                .injectValue("createdAt", member.getTimeCreated().getLong(ChronoField.INSTANT_SECONDS))
                .injectValue("joinedAt", member.getTimeJoined().getLong(ChronoField.INSTANT_SECONDS))
                .injectValue("color", EmbedColors.DEFAULT);
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

    // EVENT EMBEDS //

    public static EmbedDTO getGenericModerationEventEmbed(EmbedCache embedCache, String name, JDA jda, ModerationService.ModerationAct moderationAct, @Nullable User deleter) {
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
                .injectValue("color", EmbedColors.DEFAULT)
                .injectValue("warningColor", EmbedColors.WARNING)
                .injectValue("deleteColor", EmbedColors.ERROR);
    }

}
