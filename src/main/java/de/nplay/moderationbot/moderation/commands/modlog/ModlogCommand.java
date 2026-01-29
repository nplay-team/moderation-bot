package de.nplay.moderationbot.moderation.commands.modlog;

import com.google.inject.Inject;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.Replies.RelativeTime;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.moderation.act.model.ModerationAct;
import de.nplay.moderationbot.moderation.act.model.RevertedModerationAct;
import de.nplay.moderationbot.notes.NotesService;
import de.nplay.moderationbot.notes.NotesService.Note;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.constraints.Max;
import io.github.kaktushose.jdac.annotations.constraints.Min;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.configuration.Property;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ComponentEvent;
import io.github.kaktushose.jdac.dispatching.reply.Component;
import io.github.kaktushose.jdac.introspection.Introspection;
import io.github.kaktushose.jdac.message.placeholder.Entry;
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
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("modlog")
@Interaction
@Permissions(BotPermissions.MODERATION_READ)
public class ModlogCommand {

    private int offset = 0;
    private int limit = 5;
    private int page = 1;
    private int maxPage = 1;
    private @Nullable User user;
    private @Nullable Member member;
    private final NotesService notesService;

    @Inject
    public ModlogCommand(NotesService notesService) {
        this.notesService = notesService;
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
        if (page != null) {
            count = count == null ? limit : count;
            offset = (page - 1) * count;
            this.page = page;
            limit = count;
        }

        maxPage = (int) Math.ceil(ModerationActService.count(target) / (double) limit);
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
        Thumbnail thumbnail = Thumbnail.fromFile(avatarUrl().downloadAsFileUpload("avatar.png"));

        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("modlog"),
                Separator.createDivider(Spacing.LARGE),
                entry("target", target()),
                entry("id", target().getIdLong()),
                entry("createdAt", RelativeTime.of(target().getTimeCreated())),
                joinedAt()
        ).withAccentColor(Replies.STANDARD).add(Section.of(thumbnail, TextDisplay.of("modlog.header")));

        container.append(TextDisplay.of("modlog.notes"));
        List<Note> notes = notesService.getAll(target());
        if (!notes.isEmpty()) {
            boolean first = true;
            for (Note note : notes) {
                container.append(
                        note.toTextDisplay(event.messageResolver(), event.getUserLocale()),
                        first ? null : Separator.createInvisible(Spacing.SMALL)
                );
                first = false;
            }
        } else {
            container.add(TextDisplay.of("modlog.empty"));
        }

        container.append(TextDisplay.of("modlog.moderations"));
        List<ModerationAct> moderationActs = ModerationActService.get(target(), limit, offset);
        if (!moderationActs.isEmpty()) {
            boolean first = true;
            for (ModerationAct act : moderationActs) {
                container.append(
                        toTextDisplay(event, act),
                        first ? null : Separator.createInvisible(Spacing.SMALL)
                );
                first = false;
            }
        } else {
            container.add(TextDisplay.of("modlog.empty"));
        }

        if (!(maxPage < 2)) {
            List<SelectOption> pages = new ArrayList<SelectOption>();
            for (int i = 2; i <= maxPage && i < 26; i++) {
                pages.add(SelectOption.of("Seite " + i, Integer.toString(i)));
            }

            container.append(ActionRow.of(Component.stringSelect("selectPage").enabled(maxPage > 1).selectOptions(pages)));
            container.add(ActionRow.of(
                    Component.button("back").enabled(page > 1),
                    Component.button("next").enabled(page < maxPage)
            )).footer(
                    TextDisplay.of("modlog.pages"),
                    true,
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
    private ImageProxy avatarUrl() {
        return switch (target()) {
            case Member member -> member.getEffectiveAvatar();
            case User user -> user.getEffectiveAvatar();
            default -> throw new IllegalStateException("Unexpected value: " + target());
        };
    }

    private Entry joinedAt() {
        if (member == null) {
            return entry("joinedAt", "empty");
        }
        return entry("joinedAt", RelativeTime.of(member.getTimeJoined()));
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
            && !reverted.revertedBy().getId().equals(Introspection.scopedGet(Property.JDA).getSelfUser().getId())
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
