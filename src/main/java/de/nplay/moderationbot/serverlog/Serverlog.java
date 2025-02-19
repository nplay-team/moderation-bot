package de.nplay.moderationbot.serverlog;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.BotEvent;
import de.nplay.moderationbot.serverlog.channel.ServerlogChannelService;
import de.nplay.moderationbot.serverlog.events.ServerlogEmbedParser;
import de.nplay.moderationbot.serverlog.events.ServerlogEvents;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.LoggerFactory;

public class Serverlog {

    private Guild guild;
    private EmbedCache embedCache;

    public Serverlog(Guild guild, EmbedCache embedCache) {
        this.guild = guild;
        this.embedCache = embedCache;
    }

    @SuppressWarnings("unchecked")
    public <T extends BotEvent> void trigger(ServerlogEvents event, T botEvent) {
        LoggerFactory.getLogger(this.getClass()).info("Triggered event: {}", botEvent.getClass().getSimpleName());

        var embed = ((ServerlogEmbedParser<T>) event.getServerlogEvent()).getEmbed(embedCache, botEvent);
        var channels = ServerlogChannelService.getServerlogChannels();

        for (var channel : channels) {
            var textChannel = guild.getChannelById(TextChannel.class, channel.channelId());
            if (textChannel != null) {
                textChannel.sendMessageEmbeds(embed.toMessageEmbed()).queue();
            }
        }
    }

}
