package de.nplay.moderationbot.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;

import java.awt.Color;
import java.time.Instant;

public class DiscordAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            try (final var client = WebhookClient.withUrl(System.getenv("WEBHOOK_URL"))) {
                WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
                builder.setTitle(new WebhookEmbed.EmbedTitle("Error: %s".formatted(event.getFormattedMessage()), null))
                        .setDescription("```%s```".formatted(ThrowableProxyUtil.asString(event.getThrowableProxy())))
                        .setColor(Color.RED.getRGB())
                        .setTimestamp(Instant.now());
                client.send(builder.build());
            }
        }
    }
}
