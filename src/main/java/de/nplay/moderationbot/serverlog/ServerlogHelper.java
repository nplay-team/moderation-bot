package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.bus.BotEvent;
import de.nplay.moderationbot.config.ConfigService;
import io.github.kaktushose.jdac.components.container.SequencedContainer;
import io.github.kaktushose.jdac.message.resolver.Resolver;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public final class ServerlogHelper {

    private static final Logger log = LoggerFactory.getLogger(ServerlogHelper.class);
    private final Guild guild;
    private final ConfigService configService;
    private final Resolver<String> resolver;

    public ServerlogHelper(Guild guild, ConfigService configService, Resolver<String> resolver) {
        this.guild = guild;
        this.configService = configService;
        this.resolver = resolver;
    }

    public SequencedContainer<TextDisplay> container(BotEvent event, String key) {
        return new SequencedContainer<>(
                resolver,
                Locale.GERMAN,
                TextDisplay.of(key)
        ).entries(
                entry("type", event.type()),
                entry("target", event.target()),
                entry("issuer", event.issuer())
        ).withAccentColor(Replies.STANDARD);
    }

    public String resolve(String key, Locale locale) {
        return resolver.resolve(key, locale);
    }

    public void send(SequencedContainer<TextDisplay> container) {
        channel().ifPresent(it -> it.sendMessageComponents(container)
                .useComponentsV2()
                .setAllowedMentions(List.of())
                .complete()
        );
    }

    private Optional<TextChannel> channel() {
        Optional<TextChannel> channel = configService.get(ConfigService.BotConfig.SERVERLOG_KANAL)
                .map(guild::getTextChannelById);
        if (channel.isEmpty()) {
            log.warn("Attempted to log event to serverlog but config is not set!");
            return Optional.empty();
        }
        return channel;
    }
}
