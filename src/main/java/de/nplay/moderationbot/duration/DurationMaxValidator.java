package de.nplay.moderationbot.duration;


import com.github.kaktushose.jda.commands.dispatching.context.InvocationContext;
import com.github.kaktushose.jda.commands.dispatching.validation.Validator;
import com.github.kaktushose.jda.commands.guice.Implementation;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@Implementation.Validator(annotation = DurationMax.class)
public class DurationMaxValidator implements Validator {

    @Override
    public boolean apply(@NotNull Object argument, @NotNull Object annotation, @NotNull InvocationContext<?> context) {
        DurationMax durationMax = (DurationMax) annotation;
        return ((Duration) argument).toMillis() <= durationMax.value() * 1000;
    }

}
