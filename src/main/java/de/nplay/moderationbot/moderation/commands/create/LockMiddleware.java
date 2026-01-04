package de.nplay.moderationbot.moderation.commands.create;

import com.google.inject.Inject;
import de.nplay.moderationbot.moderation.act.ModerationActLock;
import io.github.kaktushose.jdac.dispatching.context.InvocationContext;
import io.github.kaktushose.jdac.dispatching.middleware.Middleware;
import io.github.kaktushose.jdac.guice.Implementation;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@Implementation.Middleware
public class LockMiddleware implements Middleware {

    private final ModerationActLock lock;

    @Inject
    public LockMiddleware(ModerationActLock lock) {
        this.lock = lock;
    }

    @Override
    public void accept(InvocationContext<?> context) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.event();

        if (lock.checkLocked(event.getOption("target").getAsUser(), event.getUser())) {
            context.cancel(MessageCreateData.fromContent("is locked"));
        }
    }

    @Override
    public @Nullable Collection<Class<?>> runFor() {
        return List.of(WarnCommand.class, TimeoutCommand.class, KickCommand.class, BanCommand.class);
    }
}
