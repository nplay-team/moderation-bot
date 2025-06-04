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
import java.util.regex.Pattern;

@Implementation.TypeAdapter(clazz = Duration.class)
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

        var normalizedRaw = raw.toUpperCase().replaceAll("\\s+", "");

        var years = extractValue(normalizedRaw, "Y");
        var months = extractValue(normalizedRaw, "M");
        var days = extractValue(normalizedRaw, "D");
        var hours = extractValue(normalizedRaw, "H");
        var minutes = extractValue(normalizedRaw, "MIN");
        var seconds = extractValue(normalizedRaw, "S");

        if (years != null) days = (days != null ? days : 0) + years * 365;
        if (months != null) days = (days != null ? days : 0) + months * 30; // Approximation, as months vary in length

        var parseString = "P" +
                (days != null ? days + "D" : "") +
                (hours != null || minutes != null || seconds != null ? "T" : "") +
                (hours != null ? hours + "H" : "") +
                (minutes != null ? minutes + "M" : "") +
                (seconds != null ? seconds + "S" : "");

        try {
            return Optional.of(Duration.parse(parseString));
        } catch (DateTimeParseException e) {
            log.warn("User provided invalid duration: {}", raw);
            return Optional.empty();
        }
    }

    private static Integer extractValue(String input, String unit) {
        var pattern = "(?i)(\\d+)" + (unit.equals("MIN") ? "MIN" : unit + "(?!IN)");
        var matcher = Pattern.compile(pattern).matcher(input);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

}
