package de.nplay.moderationbot.moderation.modlog;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.embeds.EmbedColors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.time.temporal.ChronoField;
import java.util.Set;

public class ModlogService {

    public static EmbedDTO getModlogEmbedHeader(EmbedCache embedCache, Member member) {
        return embedCache.getEmbed("modlogHeader")
                .injectValue("username", member.getUser().getName())
                .injectValue("userId", member.getIdLong())
                .injectValue("avatarUrl", member.getUser().getEffectiveAvatarUrl())
                .injectValue("roles", member.getRoles().stream().map(Role::getName).reduce((a, b) -> a + ", " + b).orElse("Keine Rollen"))
                .injectValue("createdAt", member.getTimeCreated().getLong(ChronoField.INSTANT_SECONDS))
                .injectValue("joinedAt", member.getTimeJoined().getLong(ChronoField.INSTANT_SECONDS))
                .injectValue("color", EmbedColors.DEFAULT);
    }

    public static Set<String> getModlogMessages() {
        return Set.of("message1", "message2", "message3");
    }
    
}
