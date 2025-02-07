package de.nplay.moderationbot.moderation.modlog;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.backend.DurationAdapter;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.time.Duration;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class ModlogEmbeds {

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

        embed.setFields(moderationActs.stream().map(it -> getField(it, jda)).toArray(EmbedDTO.Field[]::new));
        return embed;
    }

    public static EmbedDTO.Field getField(ModerationService.ModerationAct moderationAct, JDA jda) {
        List<String> bodyLines = new ArrayList<>();

        bodyLines.add("%s".formatted(moderationAct.reason()));
        bodyLines.add("-<@%s> (%s)".formatted(moderationAct.issuerId(), jda.retrieveUserById(moderationAct.issuerId()).complete().getName()));

        if (moderationAct.revokeAt() != null && !moderationAct.reverted()) {
            bodyLines.addFirst("Aktiv bis: <t:%s:f>".formatted(moderationAct.revokeAt().getTime() / 1000));
        }

        if (moderationAct.duration() != null) {
            bodyLines.addFirst("Dauer: %s".formatted(DurationAdapter.toString(Duration.ofMillis(moderationAct.duration()))));
        }

        if (moderationAct.reverted()) {
            bodyLines.addLast("*Aufgehoben am: <t:%s:f>*".formatted(moderationAct.revertedAt().getTime() / 1000));
        }

        return new EmbedDTO.Field(
                "#%s | %s | <t:%s>".formatted(moderationAct.id(), moderationAct.type().humanReadableString, moderationAct.createdAt().getTime() / 1000),
                String.join("\n", bodyLines),
                false
        );
    }

}
