package de.nplay.moderationbot.moderation.act.lock;

import com.google.inject.Inject;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.definitions.description.MethodDescription;
import io.github.kaktushose.jdac.dispatching.context.InvocationContext;
import io.github.kaktushose.jdac.dispatching.middleware.Middleware;
import io.github.kaktushose.jdac.guice.Implementation;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static de.nplay.moderationbot.NPLAYModerationBot.ERROR;
import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("lock")
@Implementation.Middleware
public class LockMiddleware implements Middleware {

    private final ModerationActLock moderationLock;

    @Inject
    public LockMiddleware(ModerationActLock moderationLock) {
        this.moderationLock = moderationLock;
    }

    @Override
    public void accept(InvocationContext<?> context) {
        MethodDescription method = context.definition().methodDescription();
        if (!method.hasAnnotation(Lock.class)) {
            return;
        }
        Lock lock = method.annotation(Lock.class);
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.event();

        User target = event.getOption(lock.value()).getAsUser();
        User moderator = event.getUser();

        if (moderationLock.checkLocked(target, moderator)) {
            context.cancel(
                    Container.of(TextDisplay.of("target-locked")).withAccentColor(ERROR),
                    entry("target", target.getAsMention()),
                    entry("moderator", moderator.getAsMention())
            );
        }
    }
}
