package de.nplay.moderationbot;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.nplay.moderationbot.moderation.act.ModerationActService;
import de.nplay.moderationbot.notes.NotesService;

public class ServiceModule extends AbstractModule {

    private final ModerationActService moderationActService;
    private final NotesService notesService;

    public ServiceModule() {
        notesService = new NotesService();
        moderationActService = new ModerationActService();
    }

    @Provides
    public NotesService notesService() {
        return notesService;
    }

    @Provides
    public ModerationActService moderationActService() {
        return moderationActService;
    }
}
