package de.nplay.moderationbot.duration;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import com.github.kaktushose.jda.commands.guice.Implementation;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

@Implementation(clazz = Duration.class)
public class DurationAdapter implements TypeAdapter<Duration> {

    @Override
    @NotNull
    public Optional<Duration> apply(@NotNull String raw, @NotNull GenericInteractionCreateEvent event) {
        return parse(raw);
    }

    public static Optional<Duration> parse(String raw) {
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

}
