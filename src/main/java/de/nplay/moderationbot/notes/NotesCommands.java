package de.nplay.moderationbot.notes;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.ReplyableEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.github.kaktushose.jda.commands.embeds.Embed;
import de.nplay.moderationbot.Helpers;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.List;

import static com.github.kaktushose.jda.commands.message.placeholder.Entry.entry;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class NotesCommands {

    private Member target;
    private boolean ephemeral = false;

    public static Embed notesEmbed(ReplyableEvent<?> event, JDA jda, UserSnowflake target, List<NotesService.Note> notes) {
        var targetUsername = jda.retrieveUserById(target.getIdLong()).complete().getName();
        var embed = event.embed("noteList").placeholders(entry("target", targetUsername));
        notes.stream().map(it -> it.toField(jda)).forEach(embed.fields()::add);
        return embed;
    }

    @Command("notes create")
    public void createNote(CommandEvent event, Member target) {
        this.target = target;
        var noteCount = NotesService.getNoteCountFromUser(target.getIdLong());
        if (noteCount >= 25) {
            event.with().embeds(event.embed("noteLimitReached")).reply();
            return;
        }
        event.replyModal("createNoteModal");
    }

    @Command(value = "Notiz erstellen", type = Type.USER)
    public void createNoteContext(CommandEvent event, Member target) {
        ephemeral = true;
        createNote(event, target);
    }

    @Command("notes list")
    public void listNotes(CommandEvent event, Member target) {
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
    public void createNoteModal(ModalEvent event,
                                @TextInput(value = "Inhalt der Notiz", style = TextInputStyle.PARAGRAPH)
                                String content) {
        var note = NotesService.createNote(target.getIdLong(), event.getMember().getIdLong(), content);

        event.with().ephemeral(ephemeral).embeds("noteCreated", entry("id", note.id()),
                entry("content", note.content()),
                entry("target", Helpers.formatUser(event.getJDA(), UserSnowflake.fromId(note.userId()))),
                entry("createdBy", Helpers.formatUser(event.getJDA(), UserSnowflake.fromId(note.creatorId()))),
                entry("createdAt", Helpers.formatTimestamp(note.createdAt()))
        ).reply();
    }
}
