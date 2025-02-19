package de.nplay.moderationbot.serverlog;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.serverlog.channel.ServerlogChannelService;
import de.nplay.moderationbot.serverlog.events.ServerlogEvent;
import de.nplay.moderationbot.serverlog.events.ServerlogEvents;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.Event;
import org.slf4j.LoggerFactory;

public class Serverlog {

    private Guild guild;
    private EmbedCache embedCache;

    public Serverlog(Guild guild, EmbedCache embedCache) {
        this.guild = guild;
        this.embedCache = embedCache;
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void trigger(ServerlogEvents event, T jdaEvent) {
        LoggerFactory.getLogger(this.getClass()).info("Triggered event: {}", jdaEvent.getClass().getSimpleName());

        var embed = ((ServerlogEvent<T>) event.getServerlogEvent()).getEmbed(embedCache, jdaEvent);
        var channels = ServerlogChannelService.getServerlogChannels();

        for (var channel : channels) {
            var textChannel = guild.getChannelById(TextChannel.class, channel.channelId());
            if (textChannel != null) {
                textChannel.sendMessageEmbeds(embed.toMessageEmbed()).queue();
            }
        }
    }

}
