package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.lifecycle.Subscriber;
import de.nplay.moderationbot.auditlog.lifecycle.events.ModerationEvent;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.config.ConfigService.BotConfig;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
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

@Bundle("serverlog")
public class ModerationSubscriber implements Subscriber<ModerationEvent> {

    private static final Logger log = LoggerFactory.getLogger(ModerationSubscriber.class);
    private final Guild guild;
    private final ConfigService configService;
    private final Resolver<String> resolver;

    public ModerationSubscriber(Guild guild, ConfigService configService, Resolver<String> resolver) {
        this.guild = guild;
        this.configService = configService;
        this.resolver = resolver;
    }

    @Override
    public void accept(ModerationEvent event) {
        Optional<TextChannel> channel = configService.get(BotConfig.SERVERLOG_KANAL).map(guild::getTextChannelById);
        if (channel.isEmpty()) {
            log.warn("Attempted to log event to serverlog but config is not set!");
            return;
        }

        SeparatedContainer container = new SeparatedContainer(
                resolver,
                TextDisplay.of("moderation"),
                Separator.createDivider(Separator.Spacing.SMALL),
                entry("type", event.type()),
                entry("id", event.act().id()),
                entry("target", event.target()),
                entry("issuer", event.issuer()),
                entry("createdAt", event.act().createdAt())
        ).withAccentColor(Replies.STANDARD);
        event.act().revokeAt().ifPresent(it -> container.add(
                TextDisplay.of("moderation.until"),
                entry("until", it)
        ));
        if (event instanceof ModerationEvent.Revert revert) {
            container.entries(
                    entry("revertingModerator", revert.act().revertedBy()),
                    entry("reason", revert.act().revertingReason()),
                    entry("revert", true)
            );
        } else if (event instanceof ModerationEvent.Delete delete) {
            container.entries(
                    entry("revertingModerator", delete.deletedBy()),
                    entry("reason", resolver.resolve("delete-reason", Locale.GERMAN)),
                    entry("revert", true)
            );
        } else {
            container.entries(entry("reason", event.act().reason()), entry("revert", false));
        }

        Helpers.sendComponentsV2(container, channel.get()).complete();
    }
}
