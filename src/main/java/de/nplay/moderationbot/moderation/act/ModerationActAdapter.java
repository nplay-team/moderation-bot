package de.nplay.moderationbot.moderation.act;

import io.github.kaktushose.jdac.dispatching.adapter.TypeAdapter;
import io.github.kaktushose.jdac.guice.Implementation;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import io.github.kaktushose.proteus.mapping.MappingResult;

@Implementation.TypeAdapter(source = Long.class, target = ModerationAct.class)
public class ModerationActAdapter implements TypeAdapter<Long, ModerationAct> {

    @Override
    public MappingResult<ModerationAct> from(Long source, MappingContext<Long, ModerationAct> context) {
        return ModerationActService.get(source)
                .map(it -> (MappingResult<ModerationAct>) MappingResult.lossless(it))
                .orElse(MappingResult.failure("Die Moderationshandlung mit der ID **#%s** existiert nicht!".formatted(source)));
    }
}
