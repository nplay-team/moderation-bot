package de.nplay.moderationbot.notes;

import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.permissions.BotPermissions;
import io.github.kaktushose.jdac.annotations.interactions.*;
import io.github.kaktushose.jdac.dispatching.events.ReplyableEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import io.github.kaktushose.jdac.embeds.Embed;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.Command.Type;

import java.util.List;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class NotesCommands {

    private static final String NOTE_ID = "note-id";
    private User target;
    private boolean ephemeral = false;

    public static Embed notesEmbed(ReplyableEvent<?> event, JDA jda, UserSnowflake target, List<NotesService.Note> notes) {
        var targetUsername = jda.retrieveUserById(target.getIdLong()).complete().getName();
        var embed = event.embed("noteList").placeholders(entry("target", targetUsername));
        notes.stream().map(it -> it.toField(jda)).forEach(embed.fields()::add);
        return embed;
    }

    @Command("notes create")
    public void createNote(CommandEvent event, User target) {
        this.target = target;
        var noteCount = NotesService.getNoteCountFromUser(target.getIdLong());
        if (noteCount >= 25) {
            event.with().embeds(event.embed("noteLimitReached")).reply();
            return;
        }
        event.replyModal("createNoteModal", Label.of("Inhalt der Notiz", TextInput.of(NOTE_ID, TextInputStyle.PARAGRAPH)));
    }

    @Command(value = "Notiz erstellen", type = Type.USER)
    public void createNoteContext(CommandEvent event, User target) {
        ephemeral = true;
        createNote(event, target);
    }

    @Command("notes list")
    public void listNotes(CommandEvent event, User target) {
        var notes = NotesService.getNotesFromUser(target.getIdLong());
        event.with().embeds(notesEmbed(event, event.getJDA(), target, notes)).reply();
    }

    @Command("notes delete")
    public void deleteNote(CommandEvent event, long noteId) {
        var note = NotesService.getNote(noteId);

        if (note.isEmpty()) {
            event.with().embeds("noteNotFound", entry("id", noteId)).reply();
            return;
        }

        NotesService.deleteNote(note.get().id());
        event.with().embeds("noteDeleted", entry("id", noteId)).reply();
    }

    @Modal("Notiz erstellen")
    public void createNoteModal(ModalEvent event) {
        var note = NotesService.createNote(target.getIdLong(), event.getMember().getIdLong(), event.value(NOTE_ID).getAsString());

        event.with().ephemeral(ephemeral).embeds("noteCreated", entry("id", note.id()),
                entry("content", note.content()),
                entry("target", Helpers.formatUser(event.getJDA(), UserSnowflake.fromId(note.userId()))),
                entry("createdBy", Helpers.formatUser(event.getJDA(), UserSnowflake.fromId(note.creatorId()))),
                entry("createdAt", Helpers.formatTimestamp(note.createdAt()))
        ).reply();
    }
}
