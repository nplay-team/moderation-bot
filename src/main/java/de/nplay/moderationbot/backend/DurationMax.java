package de.nplay.moderationbot.backend;

import com.github.kaktushose.jda.commands.annotations.constraints.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(Duration.class)
public @interface DurationMax {
    long value();

    String message() default "Die angegebene Dauer ist zu groß!";
}

