package de.nplay.moderationbot.backend;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

public class DurationAdapter implements TypeAdapter<Duration> {

    @Override
    @NotNull
    public Optional<Duration> apply(@NotNull String raw, @NotNull GenericInteractionCreateEvent event) {
        var parseString = raw.toUpperCase()
                .replaceAll("\\s+", "")
                .replaceAll("DAYS?", "D")
                .replaceAll("(?:HOURS?)|(?:HRS?)", "H")
                .replaceAll("(?:MINUTES?)|(?:MINS?)", "M")
                .replaceAll("(?:SECONDS?)|(?:SECS?)", "S")
                .replaceAll("(\\d+D)", "P$1T");


        parseString = parseString.charAt(0) != 'P' ? "PT" + parseString : parseString;
        parseString = parseString.charAt(parseString.length() - 1) == 'T' ? parseString + "0S" : parseString;

        return Optional.of(Duration.parse(parseString));
    }

    public static String toString(Duration duration) {
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
