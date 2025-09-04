package de.nplay.moderationbot.serverlog;

import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import de.nplay.moderationbot.BotEvent;
import de.nplay.moderationbot.config.ConfigService;
import net.dv8tion.jda.api.entities.Guild;

public record Serverlog(Guild guild) {

    public void onEvent(BotEvent event, ReplyableEvent<?> discordEvent) {
        var embed = event.embedSupplier().apply(discordEvent);
        var serverlogChannel = event.jda().getTextChannelById(ConfigService.get(ConfigService.BotConfig.SERVERLOG_KANAL).orElse("0"));

        if (serverlogChannel != null) {
            serverlogChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

}
