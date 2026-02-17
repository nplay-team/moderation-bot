package de.nplay.moderationbot.notes;

import com.google.inject.Inject;
import de.nplay.moderationbot.Replies;
import de.nplay.moderationbot.auditlog.lifecycle.BotEvent;
import de.nplay.moderationbot.auditlog.lifecycle.Lifecycle;
import de.nplay.moderationbot.auditlog.lifecycle.events.NoteEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.notes.NotesService.Note;
import de.nplay.moderationbot.permissions.BotPermissions;
import de.nplay.moderationbot.util.SeparatedContainer;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Modal;
import io.github.kaktushose.jdac.annotations.interactions.Permissions;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Bundle("notes")
@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class NotesCommands {

    private static final int NOTE_LIMIT = 10;
    private static final String NOTE_ID = "note-id";
    private @Nullable User target;
    private boolean ephemeral;

    private final NotesService notesService;

    @Inject
    public NotesCommands(NotesService notesService) {
        this.notesService = notesService;
    }

    @Command(value = "modal", type = Type.USER)
    public void onCreateContext(CommandEvent event, User target) {
        ephemeral = true;
        onCreate(event, target);
    }

    @Command("notes create")
    public void onCreate(CommandEvent event, User target) {
        this.target = target;

        if (notesService.count(target) >= NOTE_LIMIT) {
            event.reply(Replies.error("limit-reached"));
            return;
        }

        event.replyModal("onModal", Label.of("modal.content", TextInput.of(NOTE_ID, TextInputStyle.PARAGRAPH)));
    }

    @Modal("modal")
    public void onModal(ModalEvent event) {
        var note = notesService.create(target, event.getUser(), event.value(NOTE_ID).getAsString());

        notesService.publish(new NoteEvent(AuditlogType.NOTE_CREATE, event.getUser(), target, note));

        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("created"),
                Separator.createDivider(Separator.Spacing.SMALL),
                entry("id", note.id())
        ).withAccentColor(Replies.SUCCESS);

        container.append(TextDisplay.of("created.content"), entry("content", note.content()));
        container.append(TextDisplay.of("created.target"), entry("target", target));
        container.append(
                TextDisplay.of("created.creator"),
                entry("createdBy", event.getMember()),
                entry("createdAt", note.createdAt())
        );

        event.with().ephemeral(ephemeral).reply(container);
    }

    @Command("notes list")
    public void onList(CommandEvent event, User target) {
        List<Note> notes = notesService.getAll(target);

        SeparatedContainer container = new SeparatedContainer(
                TextDisplay.of("list"),
                Separator.createDivider(Separator.Spacing.SMALL),
                entry("target", target)
        ).withAccentColor(Replies.STANDARD);

        if (notes.isEmpty()) {
            container.append(TextDisplay.of("list.empty"));
        } else {
            notes.forEach(note -> container.append(note.toTextDisplay(event.messageResolver(), event.getUserLocale())));
        }

        event.reply(container);
    }

    @Command("notes delete")
    public void onDelete(CommandEvent event, long noteId) {
        Optional<Note> note = notesService.get(noteId);

        if (note.isEmpty()) {
            event.reply(Replies.warning("not-found"), entry("id", noteId));
            return;
        }

        notesService.publish(new NoteEvent(AuditlogType.NOTE_DELETE, event.getUser(), note.get().target(), note.get()));
        notesService.delete(noteId);

        event.reply(Replies.success("deleted"), entry("id", noteId));
    }
}
