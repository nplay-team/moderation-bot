package de.nplay.moderationbot.duration;


import com.github.kaktushose.jda.commands.dispatching.validation.Validator;
import com.github.kaktushose.jda.commands.guice.Implementation;

import java.time.Duration;

@Implementation.Validator(annotation = DurationMax.class)
public class DurationMaxValidator implements Validator<Duration, DurationMax> {

    @Override
    public void apply(Duration input, DurationMax durationMax, Context context) {
        Duration max = Duration.of(durationMax.amount(), durationMax.unit());
        if (input.compareTo(max) > 0) {
            context.fail("Die angegebene Dauer ist zu lang! Die maximale Dauer beträgt %s.".formatted(humanReadable(max)));
        }
    }

    private String humanReadable(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
