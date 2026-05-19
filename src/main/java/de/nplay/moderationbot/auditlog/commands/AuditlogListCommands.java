package de.nplay.moderationbot.auditlog.commands;

import de.nplay.moderationbot.auditlog.AuditlogService.AuditlogEntry;
import io.github.kaktushose.jdac.components.pagination.Page;
import io.github.kaktushose.jdac.components.pagination.Pagination;
import io.github.kaktushose.jdac.components.pagination.layout.Control;
import io.github.kaktushose.jdac.components.pagination.layout.ControlRow;
import io.github.kaktushose.jdac.components.pagination.layout.Dynamic;
import io.github.kaktushose.jdac.components.pagination.layout.Static;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import io.github.kaktushose.jdac.property.JDACProperty;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public class AuditlogListCommands {

    protected static final int LIMIT = 1;
    protected final Pagination pagination;
    protected int offset = 0;
    protected @Nullable Supplier<List<AuditlogEntry>> entrySupplier;

    public AuditlogListCommands(Class<? extends AuditlogListCommands> controllerClass) {
        pagination = Pagination.of(
                Static.text("pagination"),
                Static.divider(Separator.Spacing.SMALL),
                Dynamic.of(page()),
                Static.divider(Separator.Spacing.SMALL),
                ControlRow.of(
                        Control.backward(Component.button(controllerClass, "onFirst")),
                        Control.backward(Component.button(controllerClass, "onBack")),
                        Control.forward(Component.button(controllerClass, "onForth")),
                        Control.forward(Component.button(controllerClass, "onLast"))
                ),
                Static.divider(Separator.Spacing.SMALL),
                Dynamic.of(_ -> List.of(TextDisplay.of("pagination.pages")))
        );
    }

    protected void reply(ReplyableEvent<?> event) {
        event.reply(pagination, entry("current", pagination.currentPage()), entry("max", pagination.maxPages()));
    }

    private Function<Page, List<ContainerChildComponent>> page() {
        return _ -> Optional.ofNullable(entrySupplier)
                .orElse(List::of)
                .get()
                .stream()
                .map(this::toTextDisplay)
                .map(ContainerChildComponent.class::cast)
                .toList();
    }

    private TextDisplay toTextDisplay(AuditlogEntry entry) {
        return TextDisplay.of(JDACProperty.MESSAGE_RESOLVER.scopedGet().resolve(
                "type-entry",
                JDACProperty.JDA_EVENT.scopedGet().getUserLocale(),
                entry("id", entry.id()), entry("createdAt", entry.createdAt()),
                entry("issuer", entry.issuer()), entry("target", entry.target()),
                entry("type", entry.type().toString()))
        );
    }
}
