package de.nplay.moderationbot.serverlog.events;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.BotEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class ServerlogEmbedParser<T extends BotEvent> extends ListenerAdapter {
    public abstract EmbedDTO getEmbed(EmbedCache embedCache, T event);
}
