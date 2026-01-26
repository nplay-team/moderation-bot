package de.nplay.moderationbot.messagelink;

import com.google.inject.Inject;
import io.github.kaktushose.jdac.configuration.Property;
import io.github.kaktushose.jdac.dispatching.adapter.TypeAdapter;
import io.github.kaktushose.jdac.guice.Implementation;
import io.github.kaktushose.jdac.introspection.Introspection;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import io.github.kaktushose.proteus.mapping.MappingResult;

import java.util.Locale;

@Implementation.TypeAdapter(source = String.class, target = MessageLink.class)
public class MessageLinkAdapter implements TypeAdapter<String, MessageLink> {

    private final MessageResolver resolver;

    @Inject
    public MessageLinkAdapter(MessageResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public MappingResult<MessageLink> from(String source, MappingContext<String, MessageLink> context) {
        Locale locale = Introspection.scopedGet(Property.JDA_EVENT).getUserLocale().toLocale();
        return MessageLink.ofString(source)
                .map(it -> (MappingResult<MessageLink>) MappingResult.lossless(it))
                .orElse(MappingResult.failure(resolver.resolve("invalid-link", locale)));
    }
}
