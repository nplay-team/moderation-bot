package de.nplay.moderationbot.serverlog;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.BotEvent;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.bot.BotConfig;
import net.dv8tion.jda.api.entities.Guild;

public record Serverlog(Guild guild, EmbedCache embedCache) {

    public void onEvent(BotEvent event) {
        var embed = event.embedSupplier().apply(embedCache);
        var serverlogChannel = event.jda().getTextChannelById(ConfigService.get(BotConfig.SERVERLOG_KANAL.toString()).orElse("0"));

        if (serverlogChannel != null) {
            serverlogChannel.sendMessageEmbeds(embed.toMessageEmbed()).queue();
        }
    }

}
