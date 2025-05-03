package de.nplay.moderationbot;

import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Duration;

public class Helpers {

    public static final ErrorHandler USER_HANDLER = new ErrorHandler().ignore(ErrorResponse.UNKNOWN_USER, ErrorResponse.CANNOT_SEND_TO_USER);

    public static String durationToString(Duration duration) {
        StringBuilder builder = new StringBuilder();

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (days > 0) builder.append(days).append("d ");
        if (hours > 0) builder.append(hours).append("h ");
        if (minutes > 0) builder.append(minutes).append("m ");
        if (seconds > 0) builder.append(seconds).append("s ");

        return builder.toString().trim();
    }

    public static String durationToString(Duration duration, boolean detailed) {
        if (detailed) {
            return durationToString(duration)
                    .replaceAll("d", " Tag(e)")
                    .replaceAll("h", " Stunde(n)")
                    .replaceAll("m", " Minute(n)")
                    .replaceAll("s", " Sekunde(n)");
        } else {
            return durationToString(duration);
        }
    }

}
