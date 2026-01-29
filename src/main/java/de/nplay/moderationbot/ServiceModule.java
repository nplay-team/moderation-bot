package de.nplay.moderationbot;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.nplay.moderationbot.notes.NotesService;

public class ServiceModule extends AbstractModule {

    private final NotesService notesService;

    public ServiceModule() {
        notesService = new NotesService();
    }

    @Provides
    public NotesService notesService() {
        return notesService;
    }
}
