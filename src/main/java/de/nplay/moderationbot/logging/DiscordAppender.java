package de.nplay.moderationbot.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import io.github._4drian3d.jdwebhooks.Embed;
import io.github._4drian3d.jdwebhooks.WebHook;
import io.github._4drian3d.jdwebhooks.WebHookClient;

import java.awt.*;
import java.time.Instant;

public class DiscordAppender extends AppenderBase<ILoggingEvent> {

    private final WebHookClient webHookClient;

    public DiscordAppender() {
        webHookClient = WebHookClient.fromURL(System.getenv("WEBHOOK_URL"));
        ;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            return;
        }
        Embed embed = Embed.builder()
                .title("Error: %s".formatted(event.getFormattedMessage()))
                .description("```%s```".formatted(ThrowableProxyUtil.asString(event.getThrowableProxy())))
                .color(Color.RED.getRGB())
                .timestamp(Instant.now())
                .build();
        WebHook webHook = WebHook.builder().embed(embed).build();
        webHookClient.sendWebHook(webHook);
    }
}
