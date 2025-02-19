package de.nplay.moderationbot.serverlog.events.impl;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.events.ModerationDeleteEvent;
import de.nplay.moderationbot.serverlog.events.ServerlogEmbedParser;

public class ModerationDeleteEmbedParser implements ServerlogEmbedParser<ModerationDeleteEvent> {
    @Override
    public EmbedDTO getEmbed(EmbedCache embedCache, ModerationDeleteEvent event) {
        return EmbedHelpers.getGenericModerationEventEmbed(embedCache, "moderationDeleteEvent", event.getJDA(), event.getModerationAct(), event.getDeleter());
    }
}
