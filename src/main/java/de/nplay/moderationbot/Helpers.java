package de.nplay.moderationbot;

import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Duration;

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

}
