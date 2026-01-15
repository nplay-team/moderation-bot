package de.nplay.moderationbot;

import de.nplay.moderationbot.messagelink.MessageLink;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jspecify.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class Helpers {

    private static final Collection<ErrorResponse> ALLOWED_ERRORS = List.of(
            ErrorResponse.UNKNOWN_USER,
            ErrorResponse.UNKNOWN_MEMBER,
            ErrorResponse.CANNOT_SEND_TO_USER
    );

    public static void sendDM(UserSnowflake user, JDA jda, Function<PrivateChannel, MessageCreateAction> function) {
        complete(jda.retrieveUserById(user.getId()).flatMap(User::openPrivateChannel).flatMap(function));
    }

    public static <T> void complete(RestAction<T> restAction) {
        completeOpt(restAction);
    }

    public static <T> Optional<T> completeOpt(RestAction<T> restAction) {
        try {
            return Optional.of(restAction.complete());
        } catch (ErrorResponseException e) {
            if (!ALLOWED_ERRORS.contains(e.getErrorResponse())) {
                throw e;
            }
        }
        return Optional.empty();
    }

    @Deprecated
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
            return messageChannel.retrieveMessageById(messageLink.messageId()).complete();
        }
        return null;
    }

    @Deprecated
    public static String formatTimestamp(Timestamp timestamp) {
        return "%s (%s)".formatted(TimeFormat.DATE_TIME_LONG.format(timestamp.getTime()), TimeFormat.RELATIVE.atTimestamp(timestamp.getTime()));
    }

    @Deprecated
    public static String formatUser(JDA jda, UserSnowflake user) {
        if (user instanceof User resolved) {
            return "%s (%s)".formatted(resolved.getAsMention(), resolved.getEffectiveName());
        }
        return "%s (%s)".formatted(user.getAsMention(), jda.retrieveUserById(user.getId()).complete().getEffectiveName());
    }
}
