package de.nplay.moderationbot.auditlog.commands;

import com.google.inject.Inject;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import io.github.kaktushose.jdac.dispatching.adapter.TypeAdapter;
import io.github.kaktushose.jdac.guice.Implementation;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import io.github.kaktushose.jdac.property.JDACIntrospection;
import io.github.kaktushose.jdac.property.JDACProperty;
import io.github.kaktushose.proteus.mapping.MappingResult;

@Implementation.TypeAdapter(source = String.class, target = AuditlogType.class)
public class AuditLogTypeAdapter implements TypeAdapter<String, AuditlogType> {

    private final MessageResolver resolver;

    @Inject
    public AuditLogTypeAdapter(MessageResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public MappingResult<AuditlogType> from(String source, MappingContext<String, AuditlogType> context) {
        try {
            source = source.replace(" ", "_").toUpperCase();
            return MappingResult.lossless(AuditlogType.valueOf(source));
        } catch (IllegalArgumentException e) {
            return MappingResult.failure(resolver.resolve("invalid-auditlog-type", JDACIntrospection.scopedGet(JDACProperty.JDA_EVENT).getUserLocale()));
        }
    }
}
