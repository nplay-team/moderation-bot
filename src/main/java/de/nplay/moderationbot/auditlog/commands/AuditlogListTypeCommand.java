package de.nplay.moderationbot.auditlog.commands;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.AuditlogService;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Button;
import io.github.kaktushose.jdac.annotations.interactions.Choices;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Bundle("auditlog")
@Interaction("auditlog")
public class AuditlogListTypeCommand extends AuditlogListCommands {

    private final AuditlogService auditlogService;

    @Inject
    public AuditlogListTypeCommand(AuditlogService auditlogService) {
        super(AuditlogListTypeCommand.class);
        this.auditlogService = auditlogService;
    }

    public static List<String> choices() {
        return Arrays.stream(AuditlogType.values()).map(AuditlogType::toString).toList();
    }

    @Command("list type")
    public void onQueryType(CommandEvent event, @Choices(provider = "choices") AuditlogType type) {
        entrySupplier = () -> {
            return auditlogService.getAll(type, LIMIT, offset, event.getGuild());
        };

        int count = auditlogService.countByType(type);

        if (count < 1) {
            event.reply(Replies.warning("no-entries"));
            return;
        }
        pagination.maxPages((int) Math.ceil(count / (double) LIMIT));

        reply(event);
    }

    @Button(emoji = "⏮️")
    public void onFirst(ComponentEvent event) {
        offset = 0;
        pagination.firstPage();
        reply(event);
    }

    @Button(emoji = "◀\uFE0F")
    public void onBack(ComponentEvent event) {
        offset -= LIMIT;
        pagination.backward();
        reply(event);
    }

    @Button(emoji = "▶\uFE0F")
    public void onForth(ComponentEvent event) {
        offset += LIMIT;
        pagination.forward();
        reply(event);
    }

    @Button(emoji = "⏭️")
    public void onLast(ComponentEvent event) {
        // max pages starts counting on 1, offset on 0
        Optional.ofNullable(pagination.maxPages()).ifPresent(it -> offset = LIMIT * (it - 1));

        pagination.lastPage();
        reply(event);
    }
}
