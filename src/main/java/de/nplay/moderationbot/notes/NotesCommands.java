package de.nplay.moderationbot.notes;

import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import static com.github.kaktushose.jda.commands.i18n.I18n.entry;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class NotesCommands {

    private Member target;
    private boolean ephemeral = false;

    @Command(value = "notes create", desc = "Erstellt eine Notiz über einen Benutzer")
    public void createNote(CommandEvent event, @Param("Zu welchem Benutzer soll eine Notiz erstellt werden?") Member target) {
        var noteCount = NotesService.getNoteCountFromUser(target.getIdLong());

        if (noteCount >= 25) {
            event.with().embeds(event.embed("noteLimitReached")).reply();
            return;
        }

        this.target = target;
        event.replyModal("createNoteModal");
    }

    @Command(value = "Notiz erstellen", type = Type.USER)
    public void createNoteContext(CommandEvent event, User target) {
        this.target = event.getGuild().retrieveMember(target).complete();
        ephemeral = true;
        event.replyModal("createNoteModal");
    }

    @Command(value = "notes list", desc = "Listet alle Notizen eines Benutzers auf")
    public void listNotes(CommandEvent event, @Param("Welcher Benutzer soll aufgelistet werden?") Member target) {
        var notes = NotesService.getNotesFromUser(target.getIdLong());
        event.with().embeds(EmbedHelpers.getNotesEmbed(event, event.getJDA(), target, notes)).reply();
    }

    @Command(value = "notes delete", desc = "Löscht eine Notiz")
    public void deleteNote(CommandEvent event, @Param("Welche Notiz soll gelöscht werden?") Long noteId) {
        var note = NotesService.getNote(noteId);

        if (note.isEmpty()) {
            event.with().embeds("noteNotFound", entry("id", noteId)).reply();
            return;
        }

        NotesService.deleteNote(note.get().id());
        event.with().embeds("noteDeleted", entry("id", noteId)).reply();
    }

    @Modal("Notiz erstellen")
    public void createNoteModal(ModalEvent event, @TextInput(value = "Inhalt der Notiz", style = TextInputStyle.PARAGRAPH) String content) {
        var note = NotesService.createNote(target.getIdLong(), event.getMember().getIdLong(), content);
        event.with().ephemeral(ephemeral).embeds(EmbedHelpers.getNotesCreatedEmbed(event, event.getJDA(), note)).reply();
    }

}
