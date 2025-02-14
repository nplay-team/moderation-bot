package de.nplay.moderationbot;

import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.Duration;

public class Helpers {

    public static final ErrorHandler UNKNOWN_USER_HANDLER = new ErrorHandler().ignore(ErrorResponse.UNKNOWN_USER);

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

}
