package de.nplay.moderationbot.serverlog;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.BotEvent;
import de.nplay.moderationbot.serverlog.channel.ServerlogChannelService;
import de.nplay.moderationbot.serverlog.channel.ServerlogChannelService.ServerlogChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Objects;

public record Serverlog(Guild guild, EmbedCache embedCache) {

    public void onEvent(BotEvent event) {
        var embed = event.embedSupplier().apply(embedCache);

        ServerlogChannelService.getServerlogChannels().stream()
                .map(ServerlogChannel::channelId)
                .map(guild::getTextChannelById)
                .filter(Objects::nonNull)
                .map(channel -> channel.sendMessageEmbeds(embed.toMessageEmbed()))
                .forEach(RestAction::queue);
    }

}
