package de.nplay.moderationbot.auditlog.bus.events;

import de.nplay.moderationbot.auditlog.bus.BotEvent;
import de.nplay.moderationbot.auditlog.model.AuditlogType;
import de.nplay.moderationbot.notes.NotesService.Note;
import net.dv8tion.jda.api.entities.UserSnowflake;

public record NoteEvent(
        AuditlogType type, UserSnowflake issuer, UserSnowflake target, Note note
) implements BotEvent { }
