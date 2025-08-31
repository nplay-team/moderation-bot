package de.nplay.moderationbot.moderation;

import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import com.github.kaktushose.jda.commands.guice.Implementation;
import de.nplay.moderationbot.moderation.ModerationService.ModerationAct;
import io.github.kaktushose.proteus.mapping.MappingResult;
import org.jetbrains.annotations.NotNull;

@Implementation
public class ModerationActAdapter implements TypeAdapter<Long, ModerationAct> {

    @Override
    @NotNull
    public MappingResult<ModerationAct> from(@NotNull Long source, @NotNull MappingContext<Long, ModerationAct> context) {
        return ModerationService.getModerationAct(source)
                .map(it -> (MappingResult<ModerationAct>) MappingResult.lossless(it))
                .orElse(MappingResult.failure("Die Moderationshandlung mit der ID **#%s** existiert nicht!".formatted(source)));
    }
}
