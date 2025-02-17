package de.nplay.moderationbot.notes;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ModalEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.nplay.moderationbot.embeds.EmbedColors;
import de.nplay.moderationbot.embeds.EmbedHelpers;
import de.nplay.moderationbot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

@Interaction
@Permissions(BotPermissions.MODERATION_CREATE)
public class NotesCommands {

    @Inject
    EmbedCache embedCache;

    private Member target;

    @SlashCommand(value = "notes create", desc = "Erstellt eine Notiz über einen Benutzer", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void createNote(CommandEvent event, @Param("Zu welchem Benutzer soll eine Notiz erstellt werden?") Member target) {
        var noteCount = NotesService.getNoteCountFromUser(target.getIdLong());

        if (noteCount >= 25) {
            event.reply(embedCache.getEmbed("noteLimitReached").injectValue("color", EmbedColors.ERROR));
            return;
        }

        this.target = target;
        event.replyModal("createNoteModal");
    }

    @ContextCommand(value = "Notiz erstellen", type = Command.Type.USER, isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void createNoteContext(CommandEvent event, User target) {
        this.target = event.getGuild().retrieveMember(target).complete();
        event.replyModal("createNoteModal");
    }

    @SlashCommand(value = "notes list", desc = "Listet alle Notizen eines Benutzers auf", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void listNotes(CommandEvent event, @Param("Welcher Benutzer soll aufgelistet werden?") Member target) {
        var notes = NotesService.getNotesFromUser(target.getIdLong());
        event.reply(EmbedHelpers.getNotesEmbed(embedCache, event.getJDA(), target, notes));
    }

    @SlashCommand(value = "notes delete", desc = "Löscht eine Notiz", isGuildOnly = true, enabledFor = Permission.MODERATE_MEMBERS)
    public void deleteNote(CommandEvent event, @Param("Welche Notiz soll gelöscht werden?") Long noteId) {
        var note = NotesService.getNote(noteId);

        if (note.isEmpty()) {
            event.reply(embedCache.getEmbed("noteNotFound").injectValue("id", noteId).injectValue("color", EmbedColors.ERROR));
            return;
        }

        NotesService.deleteNote(note.get().id());
        event.reply(embedCache.getEmbed("noteDeleted").injectValue("id", noteId).injectValue("color", EmbedColors.SUCCESS));
    }

    @Modal("Notiz erstellen")
    public void createNoteModal(ModalEvent event, @TextInput(value = "Inhalt der Notiz", style = TextInputStyle.PARAGRAPH) String content) {
        var note = NotesService.createNote(target.getIdLong(), event.getMember().getIdLong(), content);
        event.reply(EmbedHelpers.getNotesCreatedEmbed(embedCache, event.getJDA(), note));
    }

}
