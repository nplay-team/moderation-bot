package de.nplay.moderationbot.duration;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import com.github.kaktushose.jda.commands.guice.Implementation;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Implementation(clazz = Duration.class)
public class DurationAdapter implements TypeAdapter<Duration> {

    private static final Logger log = LoggerFactory.getLogger(DurationAdapter.class);

    @Override
    @NotNull
    public Optional<Duration> apply(@NotNull String raw, @NotNull GenericInteractionCreateEvent event) {
        return parse(raw);
    }

    public static Optional<Duration> parse(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        var parseString = raw.toUpperCase()
                .replaceAll("\\s+", "")
                .replaceAll("DAYS?", "D")
                .replaceAll("(?:HOURS?)|(?:HRS?)", "H")
                .replaceAll("(?:MINUTES?)|(?:MINS?)", "M")
                .replaceAll("(?:SECONDS?)|(?:SECS?)", "S")
                .replaceAll("(\\d+D)", "P$1T");

        parseString = parseString.charAt(0) != 'P' ? "PT" + parseString : parseString;
        parseString = parseString.charAt(parseString.length() - 1) == 'T' ? parseString + "0S" : parseString;

        try {
            return Optional.of(Duration.parse(parseString));
        } catch (DateTimeParseException e) {
            log.warn("User provided invalid duration: {}", raw);
            return Optional.empty();
        }
    }

}
