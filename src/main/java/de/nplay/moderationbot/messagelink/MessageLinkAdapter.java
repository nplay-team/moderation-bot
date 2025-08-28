package de.nplay.moderationbot.messagelink;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import com.github.kaktushose.jda.commands.guice.Implementation;
import io.github.kaktushose.proteus.mapping.MappingResult;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Implementation.TypeAdapter(source = String.class, target = MessageLink.class)
public class MessageLinkAdapter implements TypeAdapter<String, MessageLink> {

    @Override
    public MappingResult<MessageLink> from(String source, MappingContext<String, MessageLink> context) {
        var result = MessageLink.ofString(source);
        if (result.isPresent()) {
            return MappingResult.lossless(result.get());
        }
        return MappingResult.failure("Der angegebene Link ist nicht gültig!");
    }
}
