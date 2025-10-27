package de.nplay.moderationbot.messagelink;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import com.github.kaktushose.jda.commands.guice.Implementation;
import io.github.kaktushose.proteus.mapping.MappingResult;

@Implementation.TypeAdapter(source = String.class, target = MessageLink.class)
public class MessageLinkAdapter implements TypeAdapter<String, MessageLink> {

    @Override
    public MappingResult<MessageLink> from(String source, MappingContext<String, MessageLink> context) {
        return MessageLink.ofString(source)
                .map(it -> (MappingResult<MessageLink>) MappingResult.lossless(it))
                .orElse(MappingResult.failure("Der angegebene Link ist nicht gültig!"));
    }
}
