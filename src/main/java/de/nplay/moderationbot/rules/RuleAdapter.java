package de.nplay.moderationbot.rules;

import com.google.inject.Inject;
import de.nplay.moderationbot.rules.RuleService.RuleParagraph;
import io.github.kaktushose.jdac.configuration.Property;
import io.github.kaktushose.jdac.dispatching.adapter.TypeAdapter;
import io.github.kaktushose.jdac.guice.Implementation;
import io.github.kaktushose.jdac.introspection.Introspection;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import io.github.kaktushose.proteus.mapping.MappingResult;

@Implementation.TypeAdapter(source = Integer.class, target = RuleParagraph.class)
public class RuleAdapter implements TypeAdapter<Integer, RuleParagraph> {

    private final RuleService ruleService;
    private final MessageResolver resolver;

    @Inject
    public RuleAdapter(RuleService ruleService, MessageResolver resolver) {
        this.ruleService = ruleService;
        this.resolver = resolver;
    }

    @Override
    public MappingResult<RuleParagraph> from(Integer source, MappingContext<Integer, RuleParagraph> context) {
        return ruleService.get(source).map(it -> (MappingResult<RuleParagraph>) MappingResult.lossless(it))
                .orElse(MappingResult.failure(resolver.resolve("invalid-rule", Introspection.scopedGet(Property.JDA_EVENT).getUserLocale())));
    }
}
