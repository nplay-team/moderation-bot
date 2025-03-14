package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.embeds.EmbedColors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.Map;
import java.util.Objects;

import static de.nplay.moderationbot.Helpers.USER_HANDLER;

public class ModerationUtils {

    public static void sendMessageToTarget(ModerationService.ModerationAct moderationAct, JDA jda, Guild guild, EmbedCache embedCache) {
        Map<String, Object> defaultInjectValues = Map.of(
                "issuerId", moderationAct.issuerId(),
                "issuerUsername", jda.retrieveUserById(moderationAct.issuerId()).complete().getName(),
                "reason", Objects.requireNonNullElse(moderationAct.reason(), "?DEL?"),
                "date", System.currentTimeMillis() / 1000,
                "paragraph", moderationAct.paragraph() != null ? moderationAct.paragraph().fullDisplay() : "?DEL?",
                "id", moderationAct.id()
        );

        EmbedDTO embedDTO = switch (moderationAct.type()) {
            case WARN -> embedCache.getEmbed("warnEmbed").injectValue("color", EmbedColors.WARNING);
            case TIMEOUT -> embedCache.getEmbed("timeoutEmbed").injectValue("color", EmbedColors.WARNING);
            case KICK -> embedCache.getEmbed("kickEmbed").injectValue("color", EmbedColors.ERROR);
            case TEMP_BAN -> embedCache.getEmbed("tempBanEmbed").injectValue("color", EmbedColors.ERROR);
            case BAN -> embedCache.getEmbed("banEmbed").injectValue("color", EmbedColors.ERROR);
        };

        embedDTO.injectValues(defaultInjectValues);

        if (moderationAct.revokeAt() != null) {
            embedDTO.injectValue("until", moderationAct.revokeAt().getTime() / 1000);
        }

        if (moderationAct.referenceMessage() != null) {
            embedDTO.injectValue("referenceMessage", "%s\n[Link](%s)".formatted(moderationAct.referenceMessage().content(), moderationAct.referenceMessage().jumpUrl(guild)));
        } else {
            embedDTO.injectValue("referenceMessage", "?DEL?");
        }

        EmbedBuilder embedBuilder = embedDTO.toEmbedBuilder();
        embedBuilder.getFields().removeIf(it -> Objects.requireNonNullElse(it.getValue(), "").contains("?DEL?"));

        jda.retrieveUserById(moderationAct.userId())
                .flatMap(User::openPrivateChannel)
                .flatMap(channel -> channel.sendMessageEmbeds(embedBuilder.build()))
                .queue(_ -> {
                }, USER_HANDLER);
    }

}
