package de.nplay.moderationbot.serverlog;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.auditlog.lifecycle.events.NoteEvent;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("serverlog")
public class NoteSubscriber extends ServerlogSubscriber<NoteEvent> {

    public NoteSubscriber(Data data) {
        super(data);
    }

    @Override
    public void accept(NoteEvent event) {
        SeparatedContainer container = container(event, "note").entries(
                entry("id", event.note().id()),
                entry("createdAt", event.note().createdAt()),
                entry("note", event.note().content())
        );
        channel().ifPresent(it -> Helpers.sendComponentsV2(container, it).complete());
    }
}
