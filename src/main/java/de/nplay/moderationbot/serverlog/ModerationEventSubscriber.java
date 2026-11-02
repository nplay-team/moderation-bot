package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.auditlog.bus.Subscriber;
import de.nplay.moderationbot.auditlog.bus.events.ModerationEvent;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.util.List;
import java.util.Locale;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("serverlog")
public class ModerationEventSubscriber implements Subscriber<ModerationEvent> {

    private final ServerlogHelper helper;

    public ModerationEventSubscriber(ServerlogHelper helper) {
        this.helper = helper;
    }

    @Override
    public void accept(ModerationEvent event) {
        var container = helper.container(event, "moderation").entries(
                entry("id", event.act().id()),
                entry("createdAt", event.act().createdAt())
        );
        container.addAll(List.of(
                TextDisplay.of("moderation.revert"),
                TextDisplay.of("moderation.reason"),
                TextDisplay.of("moderation.date")
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
                    entry("reason", helper.resolve("delete-reason", Locale.GERMAN)),
                    entry("revert", true)
            );
        } else {
            container.entries(entry("reason", event.act().reason()), entry("revert", false));
        }
        event.act().revokeAt().ifPresent(it ->
                container.add(TextDisplay.of("moderation.until"), entry("until", it))
        );

        helper.send(container);
    }
}
