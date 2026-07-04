package de.nplay.moderationbot.moderation.commands.modlog;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.Replies.RelativeTime;
import de.nplay.moderationbot.config.ConfigService;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.notes.NotesService;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.constraints.Max;
import io.github.kaktushose.jdac.annotations.constraints.Min;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.components.SequencedTextDisplay;
import io.github.kaktushose.jdac.components.container.SeparatedContainer;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import io.github.kaktushose.jdac.message.placeholder.Entry;
import io.github.kaktushose.jdac.property.JDACIntrospection;
import io.github.kaktushose.jdac.property.JDACProperty;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.separator.Separator.Spacing;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("modlog")
@Interaction
@Permissions(BotPermissions.MODERATION_READ)
public class ModlogCommand {

    private final NotesService notesService;
    private final ModerationActService actService;
    private final ConfigService configService;
    private int offset = 0;
    private int limit = 5;
    private int page = 1;
    private int maxPage = 1;
    private @Nullable User user;
    private @Nullable Member member;

    @Inject
    public ModlogCommand(NotesService notesService, ModerationActService actService, ConfigService configService) {
        this.notesService = notesService;
        this.actService = actService;
        this.configService = configService;
    }

    @Command(value = "mod log")
    public void modlog(
            CommandEvent event,
            User target,
            @Param(optional = true) @Min(1) @Nullable Integer page,
            @Param(optional = true) @Min(1) @Max(25) @Nullable Integer count
    ) {
        user = target;
        member = Helpers.completeOpt(event.getGuild().retrieveMember(target)).orElse(null);
        limit = count != null ? count : limit;

        if (page != null) {
            offset = (page - 1) * limit;
            this.page = page;
        }

        maxPage = (int) Math.ceil(actService.count(target) / (double) limit);
        if (maxPage == 0) {
            maxPage = 1;
        }

        if (this.page > maxPage) {
            this.page = maxPage;
            offset = (this.page - 1) * limit;
        }

        replyModlog(event);
    }

    @StringMenu("navigation")
    public void selectPage(ComponentEvent event, List<String> values) {
        page = Integer.parseInt(values.get(0));
        offset = (page - 1) * limit;
        event.jdaEvent().deferEdit().complete();
        replyModlog(event);
    }

    @Button("navigation.back")
    public void back(ComponentEvent event) {
        page--;
        offset -= limit;
        event.jdaEvent().deferEdit().complete();
        replyModlog(event);
    }

    @Button("navigation.next")
    public void next(ComponentEvent event) {
        page++;
        offset += limit;
        event.jdaEvent().deferEdit().complete();
        replyModlog(event);
    }

    private void replyModlog(ReplyableEvent<?> event) {
        SeparatedContainer container = SeparatedContainer.of(
                TextDisplay.of("modlog"),
                Separator.createDivider(Spacing.LARGE)
        ).entries(
                entry("target", target()),
                entry("id", target().getIdLong()),
                entry("createdAt", RelativeTime.of(target().getTimeCreated())),
                joinedAt(),
                roles(target())
        ).withAccentColor(
                Replies.STANDARD
        ).add(
                Section.of(Thumbnail.fromUrl(avatarUrl()), TextDisplay.of("modlog.header"))
        );

        SequencedTextDisplay noteDisplay = SequencedTextDisplay.of("modlog.notes");
        var notes = notesService.getAll(target());
        notes.forEach(note -> noteDisplay.add(note.toTextDisplay(event.messageResolver(), event.getUserLocale())));
        if (notes.isEmpty()) {
            noteDisplay.add("modlog.empty");
        }
        container.add(noteDisplay);

        SequencedTextDisplay moderationDisplay = SequencedTextDisplay.of("modlog.moderations");
        var moderationActs = actService.get(target(), limit, offset);
        moderationActs.forEach(act -> moderationDisplay.add(toTextDisplay(event, act)));
        if (moderationActs.isEmpty()) {
            moderationDisplay.add("modlog.empty");
        }
        container.add(moderationDisplay);

        if (maxPage > 1) {
            List<SelectOption> pages = IntStream.range(1, maxPage + 1)
                    .filter(it -> it != page)
                    .mapToObj(it -> SelectOption.of("Seite %s".formatted(it), Integer.toString(it)))
                    .toList();

            container.add(
                    ActionRow.of(Component.stringSelect("selectPage").enabled(maxPage > 1).selectOptions(pages))
            ).addUnseparated(
                    ActionRow.of(Component.button("back").enabled(page > 1), Component.button("next").enabled(page < maxPage))
            ).addLast(
                    TextDisplay.of("modlog.pages"),
                    entry("page", page),
                    entry("maxPage", maxPage)
            );
        }

        event.reply(container);
    }

    private UserSnowflake target() {
        if (member != null) {
            return member;
        }
        return Objects.requireNonNull(user);
    }

    @SuppressWarnings("PatternVariableHidesField")
    private String avatarUrl() {
        return switch (target()) {
            case Member member -> member.getEffectiveAvatarUrl();
            case User user -> user.getEffectiveAvatarUrl();
            default -> throw new IllegalStateException("Unexpected value: " + target());
        };
    }

    private Entry joinedAt() {
        if (member == null) {
            return entry("joinedAt", "empty");
        }
        return entry("joinedAt", RelativeTime.of(member.getTimeJoined()));
    }

    private Entry roles(UserSnowflake snowflake) {
        if (!(snowflake instanceof Member member)) {
            return entry("roles", "empty");
        }

        var spielersucheAusschlussRoleId = configService.get(ConfigService.BotConfig.SPIELERSUCHE_AUSSCHLUSS_ROLLE);
        if (spielersucheAusschlussRoleId.isEmpty()) {
            return entry("roles", "empty");
        }

        if (
                member
                        .getRoles()
                        .stream()
                        .noneMatch(it -> it.getId().equals(spielersucheAusschlussRoleId.get()))
        ) {
            return entry("roles", "empty");
        }

        return entry("roles", "<@&%s>".formatted(spielersucheAusschlussRoleId.get()));
    }

    private TextDisplay toTextDisplay(ReplyableEvent<?> event, ModerationAct act) {
        var entries = Entry.toMap(
                entry("id", act.id()),
                entry("type", act.type().localized(event.getUserLocale())),
                entry("createdAt", act.createdAt()),
                entry("reason", act.reason()),
                entry("issuer", act.issuer())
        );
        if (act instanceof RevertedModerationAct reverted
                && !reverted.revertedBy().getId().equals(JDACIntrospection.scopedGet(JDACProperty.JDA).getSelfUser().getId())
        ) {
            entries.putAll(Entry.toMap(
                    entry("reverter", reverted.revertedBy()),
                    entry("revertedAt", reverted.revertedAt()),
                    entry("revertingReason", reverted.revertingReason())
            ));
        }
        return TextDisplay.of(event.resolve(
                act instanceof RevertedModerationAct ? "entry.reverted" : "entry",
                entries
        ));
    }
}
