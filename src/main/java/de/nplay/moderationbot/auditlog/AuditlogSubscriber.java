package de.nplay.moderationbot.auditlog;

import de.nplay.moderationbot.auditlog.AuditlogService.AuditlogCreateData;
import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.lifecycle.Subscriber;
import de.nplay.moderationbot.auditlog.lifecycle.events.*;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload.*;

public class AuditlogSubscriber implements Subscriber<BotEvent> {

    private final AuditlogService service;

    public AuditlogSubscriber(AuditlogService service) {
        this.service = service;
    }

    @Override
    public void accept(BotEvent botEvent) {
        AuditlogPayload payload = switch (botEvent) {
            case NoteEvent event -> new NoteCreate(event.note());
            case PermissionsEvent event -> new PermissionsUpdate(event.oldPermissions(), event.newPermissions());
            case ConfigEvent event -> new ConfigUpdate(event.config(), event.oldValue(), event.newValue());
            case SlowmodeEvent event -> new SlowmodePayload(event.durationMillis());
            case ModerationEvent.Create event -> new ModerationCreate(event.act());
            case ModerationEvent.Revert event -> new ModerationRevert(event.act(), event.automatic());
            case ModerationEvent.Delete event -> new ModerationDelete(event.act().id());
            case MessagePurgeEvent event -> new MessagePurge(event);
            default -> null;
        };

        service.create(new AuditlogCreateData(botEvent.type(), botEvent.issuer(), botEvent.target(), payload));
    }
}
