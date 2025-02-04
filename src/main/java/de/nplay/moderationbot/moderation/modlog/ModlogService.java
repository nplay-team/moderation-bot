package de.nplay.moderationbot.moderation.modlog;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.moderation.ModerationService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.time.temporal.ChronoField;
import java.util.List;

public class ModlogService {

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

    public static EmbedDTO getModlogEmbed(EmbedCache embedCache, List<ModerationService.ModerationAct> moderationActs, Integer page, Integer maxPage) {
        var embed = embedCache.getEmbed("modlogActs")
                .injectValue("page", page)
                .injectValue("maxPage", maxPage)
                .injectValue("color", EmbedColors.DEFAULT);

        embed.setFields(moderationActs.stream().map(ModlogService::getField).toArray(EmbedDTO.Field[]::new));
        return embed;
    }

    public static EmbedDTO.Field getField(ModerationService.ModerationAct moderationAct) {
        return new EmbedDTO.Field(
                "#%s | %s | <t:%s>".formatted(moderationAct.id(), moderationAct.type().humanReadableString, moderationAct.createdAt().getTime() / 1000),
                "%s\n-<@%s>".formatted(moderationAct.reason(), moderationAct.issuerId()),
                false
        );
    }
    
}
