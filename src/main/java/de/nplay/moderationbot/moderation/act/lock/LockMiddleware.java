package de.nplay.moderationbot.moderation.act.lock;

import com.google.inject.Inject;
import io.github.kaktushose.jdac.definitions.description.MethodDescription;
import io.github.kaktushose.jdac.dispatching.context.InvocationContext;
import io.github.kaktushose.jdac.dispatching.middleware.Middleware;
import io.github.kaktushose.jdac.guice.Implementation;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

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

        if (moderationLock.checkLocked(event.getOption(lock.value()).getAsUser(), event.getUser())) {
            context.cancel(MessageCreateData.fromContent("is locked"));
        }
    }
}
