package de.nplay.moderationbot.notes;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.rowmapper.RowMapping;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import io.github.kaktushose.jdac.message.resolver.Resolver;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

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
                ).insertAndGetKeys();

        return getNote(result.keys().getFirst()).orElseThrow();
    }

    public static void deleteNote(long id) {
        Query.query("DELETE FROM notes WHERE id = ?")
                .single(Call.of().bind(id))
                .delete();
    }

    public record Note(
            long id,
            long userId,
            long createdBy,
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

        public TextDisplay toTextDisplay(Resolver<String> resolver, DiscordLocale locale) {
            return TextDisplay.of(resolver.resolve(
                    "list.entry",
                    locale.toLocale(),
                    entry("id", id()),
                    entry("date", new AbsoluteTime(createdAt())),
                    entry("createdBy", UserSnowflake.fromId(createdBy())),
                    entry("content", content())
            ));
        }
    }
}
