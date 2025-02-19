package de.nplay.moderationbot.serverlog.events;

import de.nplay.moderationbot.serverlog.events.impl.GenericModeration;
import de.nplay.moderationbot.serverlog.events.impl.ModerationDelete;
import net.dv8tion.jda.api.events.Event;

public enum ServerlogEvents {
    MODERATION_CREATED(new GenericModeration("moderationCreateEvent")),
    MODERATION_REVERTED(new GenericModeration("moderationRevertEvent")),
    MODERATION_DELETED(new ModerationDelete());

    private ServerlogEvent<? extends Event> eventClass;

    ServerlogEvents(ServerlogEvent<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }

    public ServerlogEvent<? extends Event> getServerlogEvent() {
        return eventClass;
    }
}
