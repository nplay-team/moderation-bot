package de.nplay.moderationbot.duration;


import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import io.github.kaktushose.jdac.dispatching.validation.Validator;
import io.github.kaktushose.jdac.guice.Implementation;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;

import java.time.Duration;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Implementation.Validator(annotation = DurationMax.class)
public class DurationMaxValidator implements Validator<Duration, DurationMax> {

    private final MessageResolver resolver;

    @Inject
    public DurationMaxValidator(MessageResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void apply(Duration input, DurationMax durationMax, Context context) {
        Duration max = Duration.of(durationMax.amount(), durationMax.unit());
        if (input.compareTo(max) > 0) {
            context.fail(resolver.resolve(
                    "duration-too-long",
                    context.invocationContext().event().getUserLocale(),
                    entry("duration", Helpers.formatDuration(max)))
            );
        }
    }
}
