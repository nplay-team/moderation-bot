package de.nplay.moderationbot.serverlog.events.impl;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.moderation.events.GenericModerationEvent;
import de.nplay.moderationbot.serverlog.events.ServerlogEvent;

public class GenericModeration extends ServerlogEvent<GenericModerationEvent> {

    private String embedName;

    public GenericModeration(String embedName) {
        this.embedName = embedName;
    }

    @Override
    public EmbedDTO getEmbed(EmbedCache embedCache, GenericModerationEvent event) {
        return EmbedHelpers.getGenericModerationEventEmbed(embedCache, embedName, event.getJDA(), event.getModerationAct(), null);
    }
}
