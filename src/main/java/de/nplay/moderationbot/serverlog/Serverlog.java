package de.nplay.moderationbot.serverlog;

import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import de.nplay.moderationbot.config.ConfigService;

public record Serverlog() {

    public void onEvent(BotEvent event, ReplyableEvent<?> discordEvent) {
        var embed = event.embedSupplier().apply(discordEvent);
        var serverlogChannel = event.jda().getTextChannelById(ConfigService.get(ConfigService.BotConfig.SERVERLOG_KANAL).orElse("0"));

        if (serverlogChannel != null) {
            serverlogChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

}
