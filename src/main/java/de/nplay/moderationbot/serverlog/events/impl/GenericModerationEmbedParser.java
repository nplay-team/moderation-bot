package de.nplay.moderationbot.serverlog.events.impl;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.events.GenericModerationEvent;
import de.nplay.moderationbot.serverlog.events.ServerlogEmbedParser;

public class GenericModerationEmbedParser extends ServerlogEmbedParser<GenericModerationEvent> {

    private String embedName;

    public GenericModerationEmbedParser(String embedName) {
        this.embedName = embedName;
    }

    @Override
    public EmbedDTO getEmbed(EmbedCache embedCache, GenericModerationEvent event) {
        return EmbedHelpers.getGenericModerationEventEmbed(embedCache, embedName, event.getJDA(), event.getModerationAct(), null);
    }
}
