package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.auditlog.bus.BotEvent;
import de.nplay.moderationbot.auditlog.bus.Subscriber;
import de.nplay.moderationbot.auditlog.bus.events.*;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;

import java.util.Objects;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("serverlog")
public class BotEventSubscriber implements Subscriber<BotEvent> {

    private final ServerlogHelper helper;

    public BotEventSubscriber(ServerlogHelper helper) {
        this.helper = helper;
    }

    public void accept(BotEvent event) {
        var container = switch (event) {
            case ConfigEvent config -> helper.container(event, "config").entries(
                    entry("config", config.config()),
                    entry("oldValue", config.oldValue()),
                    entry("newValue", config.newValue())
            );
            case MessagePurgeEvent purge -> helper.container(event, "purge").entries(
                    entry("amount", Objects.requireNonNullElse(purge.amount(), purge.pivotMessageId()))
            );
            case NoteEvent note -> helper.container(event, "note").entries(
                    entry("id", note.note().id()),
                    entry("note", note.note().content())
            );
            case PermissionsEvent permissions -> helper.container(event, "permissions").entries(
                    entry("oldValue", BotPermissions.decode(permissions.oldPermissions())),
                    entry("newValue", BotPermissions.decode(permissions.newPermissions()))
            );
            case SlowmodeEvent slowmode -> helper.container(event, "slowmode").entries(
                    entry("duration", Optional.ofNullable(slowmode.duration()).map(Helpers::formatDuration).orElse("kein Slowmode"))
            );
            case SpielersucheAusschlussEvent _, SpielersucheFreigabeEvent _ -> helper.container(event, "spielersuche");
            default -> null;
        };

        if (container == null) {
            return;
        }
        container.entries(entry("createdAt", AbsoluteTime.now()));

        helper.send(container);
    }
}
