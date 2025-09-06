package de.nplay.moderationbot.moderation.act;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import com.github.kaktushose.jda.commands.guice.Implementation;
import de.nplay.moderationbot.moderation.act.ModerationActService.ModerationAct;
import io.github.kaktushose.proteus.mapping.MappingResult;

@Implementation.TypeAdapter(source = Long.class, target = ModerationAct.class)
public class ModerationActAdapter implements TypeAdapter<Long, ModerationAct> {

    @Override
    public MappingResult<ModerationAct> from(Long source, MappingContext<Long, ModerationAct> context) {
        return ModerationActService.getModerationAct(source)
                .map(it -> (MappingResult<ModerationAct>) MappingResult.lossless(it))
                .orElse(MappingResult.failure("Die Moderationshandlung mit der ID **#%s** existiert nicht!".formatted(source)));
    }
}
