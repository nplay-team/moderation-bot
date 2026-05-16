package de.nplay.moderationbot.auditlog.commands;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.AuditlogService;
import de.nplay.moderationbot.auditlog.AuditlogService.AuditlogEntry;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Button;
import io.github.kaktushose.jdac.annotations.interactions.Choices;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.components.pagination.Page;
import io.github.kaktushose.jdac.components.pagination.Pagination;
import io.github.kaktushose.jdac.components.pagination.layout.Control;
import io.github.kaktushose.jdac.components.pagination.layout.ControlRow;
import io.github.kaktushose.jdac.components.pagination.layout.Dynamic;
import io.github.kaktushose.jdac.components.pagination.layout.Static;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.message.resolver.MessageResolver;
import io.github.kaktushose.jdac.property.JDACProperty;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.separator.Separator.Spacing;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("auditlog")
@Interaction("auditlog")
public class AuditlogListCommands {

    private static final int LIMIT = 1;
    private final AuditlogService auditlogService;
    private final Guild guild;
    private final MessageResolver resolver;
    private final DiscordLocale userLocale;
    private Pagination pagination;
    private int offset = 0;

    @Inject
    public AuditlogListCommands(AuditlogService auditlogService, Guild guild, MessageResolver resolver) {
        this.auditlogService = auditlogService;
        this.guild = guild;
        this.resolver = resolver;
        userLocale = JDACProperty.JDA_EVENT.scopedGet().getUserLocale();
    }

    public static List<String> choices() {
        return Arrays.stream(AuditlogType.values()).map(AuditlogType::toString).toList();
    }

    @Command("list type")
    public void onQueryType(CommandEvent event, @Choices(provider = "choices") AuditlogType type) {
        pagination = Pagination.of(
                Static.text("pagination"),
                Static.divider(Spacing.SMALL),
                Dynamic.of(page(type)),
                Static.divider(Spacing.SMALL),
                ControlRow.of(
                        Control.backward("onFirst"), Control.backward("onBack"),
                        Control.forward("onForth"), Control.forward("onLast")
                ),
                Static.divider(Spacing.SMALL),
                Dynamic.of(_ -> List.of(TextDisplay.of("pagination.pages")))
        );

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
        event.reply();
    }

    @Button(emoji = "⏭️")
    public void onLast(ComponentEvent event) {
        offset = LIMIT * pagination.maxPages();
        pagination.lastPage();
        reply(event);
    }

    private void reply(ReplyableEvent<?> event) {
        event.reply(pagination, entry("current", pagination.currentPage()), entry("max", pagination.maxPages()));
    }

    private Function<Page, List<ContainerChildComponent>> page(AuditlogType type) {
        return _ -> auditlogService.getAll(type, LIMIT, offset, guild)
                .stream()
                .map(this::toTextDisplay)
                .map(ContainerChildComponent.class::cast)
                .toList();
    }

    private TextDisplay toTextDisplay(AuditlogEntry entry) {
        return TextDisplay.of(resolver.resolve("type-entry", userLocale, entry("id", entry.id()),
                entry("createdAt", entry.createdAt()), entry("issuer", entry.issuer()),
                entry("target", entry.target()), entry("type", entry.type().toString())));
    }
}
