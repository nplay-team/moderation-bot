package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.auditlog.lifecycle.events.ModerationEvent;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;

import java.util.Locale;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("serverlog")
public class ModerationSubscriber extends ServerlogSubscriber<ModerationEvent> {

    public ModerationSubscriber(Data data) {
        super(data);
    }

    @Override
    public void accept(ModerationEvent event) {
        SeparatedContainer container = container(event, "moderation").entries(
                entry("id", event.act().id()),
                entry("createdAt", event.act().createdAt())
        );
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

        channel().ifPresent(it -> Helpers.sendComponentsV2(container, it).complete());
    }
}
