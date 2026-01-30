package de.nplay.moderationbot.auditlog;

import de.nplay.moderationbot.auditlog.AuditlogService.AuditlogCreateData;
import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.lifecycle.Subscriber;
import de.nplay.moderationbot.auditlog.lifecycle.events.NoteEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload.NotePayload;
import org.jspecify.annotations.Nullable;

public class AuditlogSubscriber implements Subscriber<BotEvent> {

    private final AuditlogService service;

    public AuditlogSubscriber(AuditlogService service) {
        this.service = service;
    }

    @Override
    public void accept(BotEvent botEvent) {
        AuditlogPayload payload = switch (botEvent) {
            case NoteEvent event -> noteEvent(event);
            default -> null;
        };

        service.create(new AuditlogCreateData(botEvent.type(), botEvent.issuer(), botEvent.target(), payload));
    }

    private @Nullable AuditlogPayload noteEvent(NoteEvent event) {
        return switch (event.type()) {
            case NOTE_CREATE, NOTE_DELETE -> new NotePayload(event.note());
            default -> null;
        };
    }
}
