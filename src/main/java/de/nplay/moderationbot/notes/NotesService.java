package de.nplay.moderationbot.notes;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static net.dv8tion.jda.api.entities.MessageEmbed.Field;

public class NotesService {

    public static Optional<Note> getNote(long id) {
        return Query.query("SELECT * FROM notes WHERE id = ?")
                .single(Call.of().bind(id))
                .mapAs(Note.class)
                .first();
    }

    public static List<Note> getNotesFromUser(long userId) {
        return Query.query("SELECT * FROM notes WHERE user_id = ?")
                .single(Call.of().bind(userId))
                .mapAs(Note.class)
                .all();
    }

    public static int getNoteCountFromUser(long userId) {
        return Query.query("SELECT COUNT(*) FROM notes WHERE user_id = ?")
                .single(Call.of().bind(userId))
                .mapAs(Integer.class)
                .first().orElseThrow();
    }

    public static Note createNote(long userId, long creatorId, String content) {
        var result = Query.query("INSERT INTO notes (user_id, creator_id, content, created_at) VALUES (?, ?, ?, ?)")
                .single(Call.of()
                        .bind(userId)
                        .bind(creatorId)
                        .bind(content)
                        .bind(new Timestamp(System.currentTimeMillis()))
                )
                .insertAndGetKeys();

        return getNote(result.keys().getFirst()).orElseThrow();
    }

    public static void deleteNote(long id) {
        Query.query("DELETE FROM notes WHERE id = ?")
                .single(Call.of().bind(id))
                .delete();
    }

    /**
     * Mapping of a note.
     * A note is a small text that can be created by a user belonging to another user.
     *
     * @param id        the internal id of the entry
     * @param userId    the user id of the user the note belongs to
     * @param creatorId the user id of the user who created the note
     * @param content   the content of the note
     * @param createdAt the timestamp when the note was created
     */
    public record Note(
            long id,
            long userId,
            long creatorId,
            String content,
            Timestamp createdAt
    ) {
        @MappingProvider("")
        public static RowMapping<Note> map() {
            return row -> new Note(
                    row.getLong("id"),
                    row.getLong("user_id"),
                    row.getLong("creator_id"),
                    row.getString("content"),
                    row.getTimestamp("created_at")
            );
        }

        public Field toField(JDA jda) {
            var creatorUsername = jda.retrieveUserById(creatorId()).complete().getName();
            var title = "Notiz $%s | <t:%s:F>".formatted(id(), createdAt().getTime() / 1000);
            var body = "Moderator: <@%s> (%s)\n%s".formatted(creatorId(), creatorUsername, content());
            return new Field(title, body, false);
        }
    }
}
