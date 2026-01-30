package de.nplay.moderationbot.auditlog;

import de.nplay.moderationbot.auditlog.AuditlogService.AuditlogCreateData;
import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.lifecycle.Subscriber;
import de.nplay.moderationbot.auditlog.lifecycle.events.NoteEvent;
import de.nplay.moderationbot.auditlog.lifecycle.events.PermissionsEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload.NotePayload;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload.PermissionsUpdate;

public class AuditlogSubscriber implements Subscriber<BotEvent> {

    private final AuditlogService service;

    public AuditlogSubscriber(AuditlogService service) {
        this.service = service;
    }

    @Override
    public void accept(BotEvent botEvent) {
        AuditlogPayload payload = switch (botEvent) {
            case NoteEvent event -> new NotePayload(event.note());
            case PermissionsEvent event -> new PermissionsUpdate(event.oldPermissions(), event.newPermissions());
            default -> null;
        };

        service.create(new AuditlogCreateData(botEvent.type(), botEvent.issuer(), botEvent.target(), payload));
    }
}
