package de.nplay.moderationbot.messagelink;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import com.github.kaktushose.jda.commands.guice.Implementation;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Implementation(clazz = MessageLink.class)
public class MessageLinkAdapter implements TypeAdapter<MessageLink> {

    @Override
    @NotNull
    public Optional<MessageLink> apply(@NotNull String raw, @NotNull GenericInteractionCreateEvent event) {
        try {
            return Optional.of(MessageLink.ofString(raw));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
