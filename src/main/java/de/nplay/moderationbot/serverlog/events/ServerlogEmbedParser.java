package de.nplay.moderationbot.serverlog.events;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import de.nplay.moderationbot.BotEvent;

public interface ServerlogEmbedParser<T extends BotEvent> {
    EmbedDTO getEmbed(EmbedCache embedCache, T event);
}
