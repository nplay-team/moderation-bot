package de.nplay.moderationbot.duration;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import com.github.kaktushose.jda.commands.guice.Implementation;
import io.github.kaktushose.proteus.mapping.MappingResult;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

@Implementation.TypeAdapter(source = String.class, target = Duration.class)
public class DurationAdapter implements TypeAdapter<String, Duration> {

    private static final Logger log = LoggerFactory.getLogger(DurationAdapter.class);

    @Override
    public MappingResult<Duration> from(String source, MappingContext<String, Duration> context) {
        return parse(source)
                .map(it -> (MappingResult<Duration>) MappingResult.lossless(it))
                .orElse(MappingResult.failure("Die angegebene Dauer ist ungültig. Bitte gib eine gültige Dauer an."));
    }

    public Optional<Duration> parse(@Nullable String raw) {
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

    @Nullable
    private Integer extractValue(String input, String unit) {
        var pattern = "(?i)(\\d+)" + (unit.equals("MIN") ? "MIN" : unit + "(?!IN)");
        var matcher = Pattern.compile(pattern).matcher(input);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
