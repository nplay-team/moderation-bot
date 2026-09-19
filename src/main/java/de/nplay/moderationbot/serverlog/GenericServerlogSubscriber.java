package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.bus.BotEvent;
import de.nplay.moderationbot.auditlog.bus.Subscriber;
import de.nplay.moderationbot.config.ConfigService;
import io.github.kaktushose.jdac.components.container.SeparatedContainer;
import io.github.kaktushose.jdac.message.resolver.Resolver;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public abstract class GenericServerlogSubscriber<T extends BotEvent> implements Subscriber<T> {

    protected static final Logger log = LoggerFactory.getLogger(GenericServerlogSubscriber.class);
    protected final Guild guild;
    protected final ConfigService configService;
    protected final Resolver<String> resolver;

    protected GenericServerlogSubscriber(Data data) {
        this.guild = data.guild();
        this.configService = data.configService();
        this.resolver = data.resolver();
    }

    protected Optional<TextChannel> channel() {
        Optional<TextChannel> channel = configService.get(ConfigService.BotConfig.SERVERLOG_KANAL).map(guild::getTextChannelById);
        if (channel.isEmpty()) {
            log.warn("Attempted to log event to serverlog but config is not set!");
            return Optional.empty();
        }
        return channel;
    }

    protected SeparatedContainer container(T event, String key) {
        return new SeparatedContainer(
                resolver,
                Locale.GERMAN,
                TextDisplay.of(key),
                Separator.createDivider(Separator.Spacing.SMALL)
        ).entries(
                entry("type", event.type()),
                entry("target", event.target()),
                entry("issuer", event.issuer())
        ).withAccentColor(Replies.STANDARD);
    }

    public record Data(Guild guild, ConfigService configService, Resolver<String> resolver) { }

}
