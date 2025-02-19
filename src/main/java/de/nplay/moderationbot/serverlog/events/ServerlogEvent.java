package de.nplay.moderationbot.serverlog.events;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class ServerlogEvent<T extends Event> extends ListenerAdapter {
    public abstract EmbedDTO getEmbed(EmbedCache embedCache, T event);
}
