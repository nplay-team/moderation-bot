package de.nplay.moderationbot.auditlog.commands;

import com.google.inject.Inject;
import de.nplay.moderationbot.auditlog.AuditlogService;
import de.nplay.moderationbot.auditlog.AuditlogService.AuditlogEntry;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.util.Pagination;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Button;
import io.github.kaktushose.jdac.annotations.interactions.Choices;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Bundle("auditlog")
@Interaction("auditlog")
public class AuditlogListCommand {

    private static final int LIMIT = 10;
    private final AuditlogService auditlogService;
    private Pagination<AuditlogEntry> pagination;

    @Inject
    public AuditlogListCommand(AuditlogService auditlogService) {
        this.auditlogService = auditlogService;
    }

    @Command("list type")
    public void onQueryType(CommandEvent event,
                            @Choices({
                                    "MODERATION_CREATE", "MODERATION_REVERT", "MODERATION_DELETE", "MESSAGE_PURGE",
                                    "NOTE_CREATE", "NOTE_DELETE", "PERMISSIONS_USER_UPDATE",
                                    "PERMISSIONS_ROLE_UPDATE", "CONFIG_UPDATE", "SLOWMODE_UPDATE",
                                    "SPIELERSUCHE_AUSSCHLUSS", "SPIELERSUCHE_FREIGABE"
                            }) String type
    ) {

    }

    @Command("list issuer")
    public void onQueryIssuer(CommandEvent event, User issuer,
                              @Choices({
                                      "MODERATION_CREATE", "MODERATION_REVERT", "MODERATION_DELETE", "MESSAGE_PURGE",
                                      "NOTE_CREATE", "NOTE_DELETE", "PERMISSIONS_USER_UPDATE",
                                      "PERMISSIONS_ROLE_UPDATE", "CONFIG_UPDATE", "SLOWMODE_UPDATE",
                                      "SPIELERSUCHE_AUSSCHLUSS", "SPIELERSUCHE_FREIGABE"
                              }) Optional<String> type
    ) {

    }

    @Command("list target")
    public void onQueryTarget(CommandEvent event, User target,
                              @Choices({
                                      "MODERATION_CREATE", "MODERATION_REVERT", "MODERATION_DELETE", "MESSAGE_PURGE",
                                      "NOTE_CREATE", "NOTE_DELETE", "PERMISSIONS_USER_UPDATE",
                                      "PERMISSIONS_ROLE_UPDATE", "CONFIG_UPDATE", "SLOWMODE_UPDATE",
                                      "SPIELERSUCHE_AUSSCHLUSS", "SPIELERSUCHE_FREIGABE"
                              }) Optional<String> type
    ) {
        pagination = new Pagination<AuditlogEntry>(
                10,
                supplier(target, type.map(AuditlogType::valueOf).orElse(null), event.getGuild()),
                (page, maxPage) -> TextDisplay.of("Seite (%s/%s)".formatted(page, maxPage)),
                Component.button("onNext"),
                Component.button("onPrevious"),
                LIMIT,
                null,
                TextDisplay.of("### Auditlog")
        ).separator(Pagination.SeparatorSetting.BODY);
        event.reply(pagination.current());
    }

    @Button("Weiter")
    public void onNext(ComponentEvent event) {
        event.reply(pagination.next());
    }

    @Button("Zurück")
    public void onPrevious(ComponentEvent event) {
        event.reply(pagination.previous());
    }

    private BiFunction<Integer, Integer, List<ContainerChildComponent>> supplier(UserSnowflake user, @Nullable AuditlogType type, Guild guild) {
        return (_, offset) -> auditlogService.getTarget(user, type, LIMIT, offset, guild).stream()
                .map(this::toTextDisplay)
                .map(ContainerChildComponent.class::cast)
                .toList();
    }

    private TextDisplay toTextDisplay(AuditlogEntry entry) {
        return TextDisplay.of("""
                **%d - %s**
                Issuer: %s Target: %s
                -# %s
                """.formatted(entry.id(), entry.type(), entry.issuer(), entry.target(), entry.createdAt()));
    }
}
