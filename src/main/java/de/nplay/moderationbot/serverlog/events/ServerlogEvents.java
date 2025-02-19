package de.nplay.moderationbot.serverlog.events;

import de.nplay.moderationbot.BotEvent;
import de.nplay.moderationbot.serverlog.events.impl.GenericModerationEmbedParser;
import de.nplay.moderationbot.serverlog.events.impl.ModerationDeleteEmbedParser;

public enum ServerlogEvents {
    MODERATION_CREATED(new GenericModerationEmbedParser("moderationCreateEvent")),
    MODERATION_REVERTED(new GenericModerationEmbedParser("moderationRevertEvent")),
    MODERATION_DELETED(new ModerationDeleteEmbedParser());

    private ServerlogEmbedParser<? extends BotEvent> eventClass;

    ServerlogEvents(ServerlogEmbedParser<? extends BotEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public ServerlogEmbedParser<? extends BotEvent> getServerlogEvent() {
        return eventClass;
    }
}
