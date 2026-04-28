package de.nplay.moderationbot.auditlog.commands;

import de.nplay.moderationbot.auditlog.AuditlogService;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Button;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.components.pagination.Page;
import io.github.kaktushose.jdac.components.pagination.Pagination;
import io.github.kaktushose.jdac.components.pagination.layout.Control;
import io.github.kaktushose.jdac.components.pagination.layout.ControlRow;
import io.github.kaktushose.jdac.components.pagination.layout.Dynamic;
import io.github.kaktushose.jdac.components.pagination.layout.Static;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator.Spacing;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.util.List;
import java.util.function.Function;

@Bundle("auditlog")
@Interaction
public class AuditLogQueryCommand {

    private final AuditlogService auditlogService;
    private final Pagination pagination;

    @Inject
    public AuditLogQueryCommand(AuditlogService auditlogService) {
        this.auditlogService = auditlogService;
        pagination = Pagination.of(
                Static.text("pagination"),
                Static.divider(Spacing.SMALL),
                Dynamic.of(page()),
                Static.divider(Spacing.SMALL),
                ControlRow.of(Control.backward("onBack"), Control.forward("onForth"))
        );
    }

    @Command("auditlog query")
    public void onQuery(CommandEvent event) {
        event.reply(pagination);
    }

    @Button(value = "pagination.back", emoji = "◀\uFE0F")
    public void onBack(ComponentEvent event) {
        event.reply(pagination.backward());
    }


    @Button(value = "pagination.forth", emoji = "▶\uFE0F")
    public void onForth(ComponentEvent event) {
        event.reply(pagination.forward());
    }

    private Function<Page, List<ContainerChildComponent>> page() {
        return page -> {
            // TODO
            return List.of(TextDisplay.of("Page " + page.currentPage()));
        };
    }
}
