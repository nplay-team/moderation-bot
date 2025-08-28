package de.nplay.moderationbot.duration;


import com.github.kaktushose.jda.commands.dispatching.validation.Validator;
import com.github.kaktushose.jda.commands.guice.Implementation;

import java.time.Duration;

@Implementation.Validator(annotation = DurationMax.class)
public class DurationMaxValidator implements Validator<Duration, DurationMax> {

    @Override
    public void apply(Duration argument, DurationMax annotation, Context context) {
       if (argument.toMillis() > annotation.value() * 1000) {
           // TODO output max allowed duration
           context.fail("Die angegebene Dauer ist zu lang!");
       }
    }
}
