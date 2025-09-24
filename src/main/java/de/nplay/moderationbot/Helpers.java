package de.nplay.moderationbot;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.embeds.Embed;
import de.nplay.moderationbot.messagelink.MessageLink;
import de.nplay.moderationbot.notes.NotesService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jspecify.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import static com.github.kaktushose.jda.commands.message.i18n.I18n.entry;

public class Helpers {

    public static final ErrorHandler USER_HANDLER = new ErrorHandler().ignore(
            ErrorResponse.UNKNOWN_USER,
            ErrorResponse.UNKNOWN_MEMBER,
            ErrorResponse.CANNOT_SEND_TO_USER
    );

    public static void sendDM(UserSnowflake user, JDA jda, Function<PrivateChannel, MessageCreateAction> function) {
        jda.retrieveUserById(user.getId())
                .flatMap(User::openPrivateChannel)
                .flatMap(function)
                .queue(null, USER_HANDLER);
    }

    public static String formatDuration(Duration duration) {
        StringBuilder builder = new StringBuilder();
        long days = duration.toDays();
        long years = days >= 365 ? Math.floorDiv(days, 365) : 0;
        long months = days >= 30 ? Math.floorDiv(duration.toDays(), 30) - years * 12 : 0;
        builder.append(format(years, "Jahr", "e"))
                .append(format(months, "Monat", "e"))
                .append(format(days - (years * 365) - (months * 30), "Tag", "e"))
                .append(format(duration.toHoursPart(), "Stunde", "n"))
                .append(format(duration.toMinutesPart(), "Minute", "n"))
                .append(format(duration.toSecondsPart(), "Sekunde", "n"));
        return builder.toString().trim();
    }

    private static String format(long value, String unit, String plural) {
        if (value > 0) {
            return "%d %s%s ".formatted(value, unit, value > 1 ? plural : "");
        }
        return "";
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

    public static String formatTimestamp(Timestamp timestamp) {
        return "%s (%s)".formatted(TimeFormat.DATE_TIME_LONG.format(timestamp.getTime()), TimeFormat.RELATIVE.atTimestamp(timestamp.getTime()));
    }

    public static String formatUser(JDA jda, UserSnowflake user) {
        if (user instanceof User resolved) {
            return "%s (%s)".formatted(resolved.getAsMention(), resolved.getEffectiveName());
        }
        return "%s (%s)".formatted(user.getAsMention(), jda.retrieveUserById(user.getId()).complete().getEffectiveName());
    }
}
