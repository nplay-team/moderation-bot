package de.nplay.moderationbot.auditlog;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.AuditlogService.AuditlogEntry;
import de.nplay.moderationbot.auditlog.model.AuditlogPayload;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("auditlog")
@Interaction("auditlog")
public class AuditlogCommands {

    private final AuditlogService auditlogService;

    @Inject
    public AuditlogCommands(AuditlogService auditlogService) {
        this.auditlogService = auditlogService;
    }

    @Command("detail")
    public void onDetail(CommandEvent event, long id) {
        Optional<AuditlogEntry> entry = auditlogService.get(id, event.getGuild());

        if (entry.isEmpty()) {
            event.reply(Replies.error("not-found"), entry("id", id));
            return;
        }

        AuditlogEntry auditlog = entry.get();
        event.reply(
                Replies.standard("entry"),
                entry("type", auditlog.type()),
                entry("createdAt", auditlog.createdAt()),
                entry("issuer", auditlog.issuer()),
                entry("target", auditlog.target()),
                entry("payload", auditlog.payload().flatMap(AuditlogPayload::toPrettyJson).orElse("no payload"))
        );
    }
}
