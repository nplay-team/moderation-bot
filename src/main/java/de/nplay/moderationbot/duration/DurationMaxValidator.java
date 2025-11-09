package de.nplay.moderationbot.duration;


import io.github.kaktushose.jdac.dispatching.validation.Validator;
import io.github.kaktushose.jdac.guice.Implementation;
import de.nplay.moderationbot.Helpers;

import java.time.Duration;

@Implementation.Validator(annotation = DurationMax.class)
public class DurationMaxValidator implements Validator<Duration, DurationMax> {

    @Override
    public void apply(Duration input, DurationMax durationMax, Context context) {
        Duration max = Duration.of(durationMax.amount(), durationMax.unit());
        if (input.compareTo(max) > 0) {
            context.fail("Die angegebene Dauer ist zu lang! Die maximale Dauer beträgt %s.".formatted(Helpers.formatDuration(max)));
        }
    }
}
