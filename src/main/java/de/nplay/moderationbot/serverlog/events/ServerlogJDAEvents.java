package de.nplay.moderationbot.serverlog.events;

import de.nplay.moderationbot.serverlog.Serverlog;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerlogJDAEvents extends ListenerAdapter {

    private Serverlog serverlog;

    public ServerlogJDAEvents(Serverlog serverlog) {
        this.serverlog = serverlog;
    }

    // All JDA events which should be picked up by the serverlog
    // Every event must be a child of GenericGuildEvent or GenericMessageEvent

    // ...

}
