package de.nplay.moderationbot.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import de.nplay.moderationbot.Replies;
import io.github._4drian3d.jdwebhooks.component.Component;
import io.github._4drian3d.jdwebhooks.webhook.WebHookClient;

import java.time.Instant;

public class DiscordAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        if (!event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            return;
        }

        String webhookUrl = System.getenv("WEBHOOK_URL");
        if (webhookUrl == null) {
            return;
        }
        WebHookClient client = WebHookClient.fromURL(webhookUrl);

        client.executeWebHook(builder -> builder.component(
                Component.container().components(
                        Component.textDisplay("### Error: %s".formatted(event.getFormattedMessage())),
                        Component.textDisplay("```%s```".formatted(ThrowableProxyUtil.asString(event.getThrowableProxy()))),
                        Component.textDisplay("-# %s".formatted(Instant.now().toString()))
                ).accentColor(Replies.ERROR.getRGB() & 0xFFFFFF).build() // remove alpha channel
        ));
    }
}
