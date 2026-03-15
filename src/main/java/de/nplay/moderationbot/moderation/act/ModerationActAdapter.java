package de.nplay.moderationbot.moderation.act;

import com.google.inject.Inject;
import io.github.kaktushose.jdac.configuration.Property;
import io.github.kaktushose.jdac.dispatching.adapter.TypeAdapter;
import io.github.kaktushose.jdac.guice.Implementation;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import io.github.kaktushose.jdac.introspection.Introspection;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import io.github.kaktushose.proteus.mapping.MappingResult;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Implementation.TypeAdapter(source = Long.class, target = ModerationAct.class)
public class ModerationActAdapter implements TypeAdapter<Long, ModerationAct> {

    private final MessageResolver resolver;
    private final ModerationActService actService;

    @Inject
    public ModerationActAdapter(MessageResolver resolver, ModerationActService actService) {
        this.resolver = resolver;
        this.actService = actService;
    }

    @Override
    public MappingResult<ModerationAct> from(Long source, MappingContext<Long, ModerationAct> context) {
        return actService.get(source)
                .map(it -> (MappingResult<ModerationAct>) MappingResult.lossless(it))
                .orElse(MappingResult.failure(resolver.resolve("invalid-act", Introspection.scopedGet(Property.JDA_EVENT).getUserLocale(), entry("id", source))));
    }
}
