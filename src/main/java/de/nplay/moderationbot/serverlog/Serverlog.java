package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.config.ConfigService;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;

public record Serverlog(ConfigService configService) {

    public void onEvent(BotEvent event, ReplyableEvent<?> discordEvent) {
        var embed = event.embedSupplier().apply(discordEvent);
        var serverlogChannel = event.jda().getTextChannelById(configService.get(ConfigService.BotConfig.SERVERLOG_KANAL).orElse("0"));

        if (serverlogChannel != null) {
            serverlogChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

}
