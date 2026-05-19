package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.lifecycle.events.*;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;

import java.util.Objects;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("serverlog")
public class BotEventSubscriber extends ServerlogSubscriber<BotEvent> {

    public BotEventSubscriber(Data data) {
        super(data);
    }

    @Override
    public void accept(BotEvent event) {
        SeparatedContainer container = switch (event) {
            case ConfigEvent config -> container(event, "config").entries(
                    entry("config", config.config()),
                    entry("oldValue", config.oldValue()),
                    entry("newValue", config.newValue())
            );
            case MessagePurgeEvent purge -> container(event, "purge").entries(
                    entry("amount", Objects.requireNonNullElse(purge.amount(), purge.pivotMessageId()))
            );
            case NoteEvent note -> container(event, "note").entries(
                    entry("id", note.note().id()),
                    entry("note", note.note().content())
            );
            case PermissionsEvent permissions -> container(event, "permissions").entries(
                    entry("oldValue", BotPermissions.decode(permissions.oldPermissions())),
                    entry("newValue", BotPermissions.decode(permissions.newPermissions()))
            );
            case SlowmodeEvent slowmode -> container(event, "slowmode").entries(
                    entry("duration", Optional.ofNullable(slowmode.duration()).map(Helpers::formatDuration).orElse("kein Slowmode"))
            );
            case SpielersucheAusschlussEvent _, SpielersucheFreigabeEvent _ -> container(event, "spielersuche");
            default -> null;
        };

        if (container == null) {
            return;
        }
        container.entries(entry("createdAt", AbsoluteTime.now()));

        channel().ifPresent(it -> Helpers.sendComponentsV2(container, it).complete());
    }
}
