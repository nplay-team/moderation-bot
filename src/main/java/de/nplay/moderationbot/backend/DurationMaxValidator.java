package de.nplay.moderationbot.backend;

import com.github.kaktushose.jda.commands.annotations.Implementation;
import com.github.kaktushose.jda.commands.dispatching.context.InvocationContext;
import com.github.kaktushose.jda.commands.dispatching.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@Implementation(annotation = DurationMax.class)
public class DurationMaxValidator implements Validator {

    @Override
    public boolean apply(@NotNull Object argument, @NotNull Object annotation, @NotNull InvocationContext<?> context) {
        DurationMax durationMax = (DurationMax) annotation;
        return ((Duration) argument).toMillis() <= durationMax.value() * 1000;
    }

}
