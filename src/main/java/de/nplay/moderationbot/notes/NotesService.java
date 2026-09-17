package de.nplay.moderationbot.notes;

import de.chojo.sadu.mapper.annotation.MappingProvider;
import de.chojo.sadu.mapper.wrapper.Row;
import de.chojo.sadu.queries.api.call.Call;
import de.chojo.sadu.queries.api.query.Query;
import de.nplay.moderationbot.Replies.AbsoluteTime;
import de.nplay.moderationbot.auditlog.bus.EventBus;
import de.nplay.moderationbot.auditlog.bus.events.NoteEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import io.github.kaktushose.jdac.annotations.i18n.Bundle;
import io.github.kaktushose.jdac.message.resolver.Resolver;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static io.github.kaktushose.jdac.message.placeholder.Entry.entry;

public class NotesService {

    private final EventBus eventBus;

    public NotesService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Optional<Note> get(long id) {
        return Query.query("SELECT * FROM notes WHERE id = ?")
                .single(Call.of().bind(id))
                .mapAs(Note.class)
                .first();
    }

    public List<Note> getAll(UserSnowflake target) {
        return Query.query("SELECT * FROM notes WHERE user_id = ?")
                .single(Call.of().bind(target.getIdLong()))
                .mapAs(Note.class)
                .all();
    }

    public int count(UserSnowflake target) {
        return Query.query("SELECT COUNT(*) FROM notes WHERE user_id = ?")
                .single(Call.of().bind(target.getIdLong()))
                .mapAs(Integer.class)
                .first().orElseThrow();
    }

    public Note create(UserSnowflake target, UserSnowflake issuer, String content) {
        var result = Query.query("INSERT INTO notes (user_id, creator_id, content, created_at) VALUES (?, ?, ?, ?)")
                .single(Call.of()
                        .bind(target.getIdLong())
                        .bind(issuer.getIdLong())
                        .bind(content)
                        .bind(new Timestamp(System.currentTimeMillis()))
                ).insertAndGetKeys();

        Note note = get(result.keys().getFirst()).orElseThrow();
        eventBus.publish(new NoteEvent(AuditlogType.NOTE_CREATE, issuer, target, note));
        return note;
    }

    public void delete(Note note, UserSnowflake issuer) {
        Query.query("DELETE FROM notes WHERE id = ?")
                .single(Call.of().bind(note.id()))
                .delete();
        eventBus.publish(new NoteEvent(AuditlogType.NOTE_DELETE, issuer, note.target(), note));
    }

    public record Note(long id, UserSnowflake target, UserSnowflake issuer, String content, AbsoluteTime createdAt) {

        @MappingProvider("")
        public Note(Row row) throws SQLException {
            this(
                    row.getLong("id"),
                    UserSnowflake.fromId(row.getLong("user_id")),
                    UserSnowflake.fromId(row.getLong("creator_id")),
                    row.getString("content"),
                    new AbsoluteTime(row.getTimestamp("created_at"))
            );
        }

        @Bundle("notes")
        public TextDisplay toTextDisplay(Resolver<String> resolver, DiscordLocale locale) {
            return TextDisplay.of(resolver.resolve(
                    "list.entry",
                    locale,
                    entry("id", id()),
                    entry("date", createdAt()),
                    entry("createdBy", issuer()),
                    entry("content", content())
            ));
        }
    }
}
