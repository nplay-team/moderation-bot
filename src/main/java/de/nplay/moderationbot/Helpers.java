package de.nplay.moderationbot;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.embeds.Embed;
import de.nplay.moderationbot.NPLAYModerationBot.EmbedColors;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.moderation.act.ModerationActService.ModerationAct;
import de.nplay.moderationbot.notes.NotesService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

public class Helpers {

    public static final ErrorHandler USER_HANDLER = new ErrorHandler().ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.UNKNOWN_MEMBER, ErrorResponse.CANNOT_SEND_TO_USER);

    public static String durationToString(Duration duration) {
        StringBuilder builder = new StringBuilder();

        long years = duration.toDays() >= 365 ? Math.floorDiv(duration.toDays(), 365) : 0;
        long months = duration.toDays() >= 30 ? Math.floorDiv(duration.toDays(), 30) : 0;
        long days = duration.toDays() - (years * 365) - (months * 30);
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (years > 0) builder.append(years).append("y ");
        if (months > 0) builder.append(months).append("m ");
        if (days > 0) builder.append(days).append("d ");
        if (hours > 0) builder.append(hours).append("h ");
        if (minutes > 0) builder.append(minutes).append("min ");
        if (seconds > 0) builder.append(seconds).append("s ");

        return builder.toString().trim();
    }

    public static String durationToString(Duration duration, boolean detailed) {
        if (detailed) {
            return durationToString(duration)
                    .replaceAll("y", " Jahr(e)")
                    .replaceAll("m ", " Monat(e) ")
                    .replaceAll("d", " Tag(e)")
                    .replaceAll("h", " Stunde(n)")
                    .replaceAll("min", " Minute(n)")
                    .replaceAll("s", " Sekunde(n)");
        } else {
            return durationToString(duration);
        }
    }

    @Nullable
    public static Message retrieveMessage(ReplyableEvent<?> event, @Nullable MessageLink messageLink) {
        if (messageLink == null || !event.isFromGuild()) {
            return null;
        }

        var guildChannel = event.getGuild().getGuildChannelById(messageLink.channelId());
        if (guildChannel instanceof MessageChannel messageChannel) {
            messageChannel.retrieveMessageById(messageLink.messageId()).complete();
        }
        return null;
    }

    public static void sendModerationToTarget(ModerationAct moderationAct, ReplyableEvent<?> event) {
        Map<String, Object> placeholders = new HashMap<>(Map.of(
                "issuerId", moderationAct.issuerId(),
                "issuerUsername", event.getJDA().retrieveUserById(moderationAct.issuerId()).complete().getName(),
                "reason", Objects.requireNonNullElse(moderationAct.reason(), "?DEL?"),
                "date", System.currentTimeMillis() / 1000,
                "paragraph", moderationAct.paragraph() != null ? moderationAct.paragraph().fullDisplay() : "?DEL?",
                "id", moderationAct.id(),
                "until", moderationAct.revokeAt() != null ? moderationAct.revokeAt().getTime() / 1000 : "?DEL?",
                "referenceMessage", moderationAct.referenceMessage() != null
                        ? moderationAct.referenceMessage().jumpUrl(event.getGuild())
                        : "?DEL?"
        ));

        Embed embed = event.embed("moderationActTargetInfo");

        switch (moderationAct.type()) {
            case WARN -> {
                placeholders.put("title", "Verwarnung");
                placeholders.put("description", "Dir wurde eine Verwarnung auf dem **NPLAY** Discord Server ausgesprochen!");
                placeholders.put("color", EmbedColors.WARNING);
            }
            case TIMEOUT -> {
                placeholders.put("title", "Timeout");
                placeholders.put("description", "Dir wurde ein Timeout auf dem **NPLAY** Discord Server auferlegt!");
                placeholders.put("color", EmbedColors.WARNING);
            }
            case KICK -> {
                placeholders.put("title", "Kick");
                placeholders.put("description", "Du wurdest vom **NPLAY** Discord Server gekickt!");
                placeholders.put("color", EmbedColors.ERROR);
            }
            case TEMP_BAN -> {
                placeholders.put("title", "Temporärer Bann");
                placeholders.put("description", "Du wurdest temporär vom **NPLAY** Discord Server gebannt!");
                placeholders.put("color", EmbedColors.ERROR);
            }
            case BAN -> {
                placeholders.put("title", "Bann");
                placeholders.put("description", "Du wurdest vom **NPLAY** Discord Server gebannt!");
                placeholders.put("color", EmbedColors.ERROR);
            }
        }
        embed.placeholders(placeholders).fields().remove("?DEL?");

        event.getJDA().retrieveUserById(moderationAct.userId())
                .flatMap(User::openPrivateChannel)
                .flatMap(channel -> channel.sendMessageEmbeds(embed.build()))
                .queue(_ -> {
                }, USER_HANDLER);
    }

    public static Embed notesEmbed(ReplyableEvent<?> event, JDA jda, UserSnowflake target, List<NotesService.Note> notes) {
        var targetUsername = jda.retrieveUserById(target.getIdLong()).complete().getName();
        var embed = event.embed("noteList").placeholders(entry("target", targetUsername));
        notes.stream().map(it -> it.toField(jda)).forEach(embed.fields()::add);
        return embed;
    }

}
