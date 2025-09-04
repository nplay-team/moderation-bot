package de.nplay.moderationbot.duration;

import com.github.kaktushose.jda.commands.annotations.constraints.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(Duration.class)
public @interface DurationMax {
    long amount();
    ChronoUnit unit();
}

